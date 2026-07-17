# RASP 绕过全链路重构设计

**日期**: 2026-07-17  
**状态**: 已批准（用户确认路线 A + 全链路一体化）  
**范围**: Java 载荷下 `RaspBypass` 客户端插件 + 目标端模块 + 构建投递  
**非目标**: 独立 attachable native agent（路线 C）、MCP 挂到载荷插件栏、未授权实网打点

---

## 1. 背景与问题

### 现状

| 文件 | 规模 | 职责 |
|------|------|------|
| `src/shells/plugins/java/RaspBypass.java` | ~845 行 | GUI：命令执行 / 禁用 RASP / 内存马 / JNI / 辅助工具 |
| `src/shells/plugins/java/assets/RaspBypassModule.java` | ~2417 行 | 目标端单体：探测、执行、禁用、内存马混杂 |
| `src/shells/plugins/java/assets/RaspBypassUtils.java` | ~378 行 | 进阶技巧；多处半成品（如 Instrumentation 获取恒为 null） |
| `native/rasp_bypass_jni.c` | ~457 行 | JNI 原生执行（Windows 优先 CreateProcess 避开 cmd.exe） |

### 结构性缺陷

1. **目标端单体过大**：技巧、探测、禁用、内存马挤在一个类，难测、难扩厂商。
2. **结果不可机读**：大量字符串拼接，UI 与自动降级无法可靠解析「哪种策略失败了」。
3. **「自动」偏弱**：`opsEnvironment` 后仅关键字建议，未形成有序策略链 + 验证闭环。
4. **半成品能力**：Utils 中 bootstrap / shellcode 等路径不完整，易造成「有按钮但无效」。
5. **构建易脱节**：UI 依赖 `assets/RaspBypassModule.classs`；源码改完未 compile 则线上仍是旧字节码。
6. **ASM 多态与 JNI 约束冲突风险**：随机改名会破坏 JNI 符号；现注释已要求禁止改 FQN。

### 目标（成功标准）

- **全链路一体化**：Detect → Plan → Exec → Verify；（可选）Disable / Memshell 由用户显式触发。
- **实战可诊断**：每次调用返回结构化结果（成功策略、tried 链、evidence、nextHint）。
- **可维护**：按能力拆模块；门面薄；旧 `evalFunc` 方法名短期兼容。
- **投递简单（MVP）**：仍以单个（或少量）`.classs` include 为主，不引入独立 agent 进程。

---

## 2. 决策摘要

| 项 | 选择 |
|----|------|
| 失败叙事 | 整体偏弱 → 大重构，非单点热修 |
| 首要场景 | 全链路一体化（非只修 exec 或只修 disable） |
| 架构路线 | **A. 管线 + 插件化模块**（否决 B 单体重写内核、C 双通道 native agent） |
| 协议 | JSON Request / Result；旧字符串可作 fallback |
| 投递 MVP | 源码拆分，编译期合并/同包；单 `RaspBypassModule.classs` 兼容名 |
| Disable/Memshell | 不进自动 pipeline，高级 Tab 手动 |

---

## 3. 架构

```
┌─────────────────────────────────────────────────┐
│  RaspBypass UI（客户端插件）                      │
│  Tab: 诊断 | 一键执行 | 高级(禁用/内存马/JNI/工具) │
└───────────────────────┬─────────────────────────┘
                        │ payload.include / evalFunc
                        │ 统一 JSON 请求/响应
┌───────────────────────▼─────────────────────────┐
│  Facade（目标端薄门面）                            │
│  兼容类名: RaspBypassModule                       │
│  路由 action → 子服务；串 pipeline；缓存 Detect    │
└───────┬─────────────┬─────────────┬─────────────┘
        │             │             │
   DetectService  ExecService   DisableService
   PlanService    VerifyService MemshellService
                  JniBridge
```

### 原则

1. **门面薄、能力厚**：Facade 不含具体 bypass 算法。
2. **统一 Result**：UI、自动降级、日志共用同一 schema。
3. **有序降级**：Exec 按 Plan 策略链尝试，记录 evidence。
4. **按能力拆源码**：Detect / Exec / Disable / Memshell / Jni 边界清晰。
5. **兼容旧入口**：`execCommand` / `opsEnvironment` 等转发到新管线。

---

## 4. 目标端包结构

```
shells/plugins/java/assets/rasp/
  RaspBypassFacade.java          # 或 RaspBypassModule 作兼容外壳
  protocol/
    RaspRequest.java
    RaspResult.java
    Stage.java                   # DETECT | PLAN | EXEC | VERIFY | DISABLE | MEMSHELL
  detect/
    EnvProbe.java                # OS / JDK / 容器 / javaagent / 粗粒度进程
    RaspFingerprint.java         # OpenRASP / JRASP / Elkeid / 青藤 / 腾讯 / 阿里 / 通用
    DetectService.java
  plan/
    ExecStrategy.java            # 策略枚举 + 适用条件
    PlanService.java             # Detect → 有序策略列表
  exec/
    ExecService.java
    strategies/
      SoftDegradeExec.java
      ProcessBuilderExec.java
      UnsafeForkAndExec.java
      ProcessImplDirectExec.java
      ThreadContextExec.java
      FinalizeExec.java
      TomcatJniExec.java
      ReflectExec.java
      JniNativeExec.java
  verify/
    VerifyService.java           # 无害回显标记校验
  disable/
    DisableService.java
    vendors/
      OpenRaspDisabler.java
      JraspDisabler.java
      GenericAgentDisabler.java
      SecurityManagerClear.java
  memshell/
    MemshellService.java         # 从旧 Module 迁入 Tomcat/Spring/Jetty/Anonymous
  jni/
    JniBridge.java
  util/
    ReflectUtil.java
    OsCmd.java
    Text.java
```

**打包策略（MVP）**：源码按上表拆分；发布时编译为 **保留公共入口类名 `RaspBypassModule`** 的字节码产物（内部可同包多 class，按依赖顺序 include，或编译期可接受的合并方案）。客户端资源名继续 `assets/RaspBypassModule.classs` 以兼容现有加载逻辑。

---

## 5. 协议

### 5.1 请求（逻辑模型）

```json
{
  "action": "pipeline | detect | plan | exec | verify | disable | memshell | jni | ping",
  "cmd": "whoami",
  "methodIndex": -1,
  "auto": true,
  "raspType": "auto",
  "disableMode": "hook|config|uninstall",
  "memshellType": "tomcatFilter",
  "path": "",
  "timeoutMs": 15000
}
```

- `methodIndex == -1` 或 `auto == true`：走 Plan 策略链。
- 指定 `methodIndex`：高级 Tab 单策略（与现有下拉序号对齐映射表）。
- 旧调用可继续用 `ReqParameter` 键：`cmd` / `cmdLine` / `methodIndex` / `autoDetect`；Facade 适配为内部 Request。

### 5.2 响应 `RaspResult`

```json
{
  "ok": true,
  "code": "OK | BLOCKED | NO_MODULE | UNSUPPORTED | ERROR",
  "stage": "DETECT|PLAN|EXEC|VERIFY|DISABLE|MEMSHELL",
  "strategy": "UnsafeForkAndExec",
  "tried": ["SoftDegrade", "ProcessBuilder", "UnsafeForkAndExec"],
  "detect": {
    "os": "Linux",
    "java": "1.8.0_392",
    "container": "Tomcat",
    "agents": ["openrasp"],
    "rasp": [
      { "vendor": "OpenRASP", "confidence": 0.9, "signals": ["class:...", "prop:..."] }
    ]
  },
  "output": "root\n",
  "evidence": [
    "ProcessBuilder: blocked_or_empty",
    "UnsafeForkAndExec: exit=0"
  ],
  "nextHint": "执行已通；可选 disable OpenRASP hook",
  "error": null,
  "raw": null
}
```

### 5.3 旧方法名兼容

| 旧 evalFunc 名 | 新行为 |
|----------------|--------|
| `opsEnvironment` | DetectService；正文可读文本 + 可选尾部/整包 JSON |
| `execCommand` | auto 则 pipeline 的 exec 段；否则单策略 |
| `checkRasp` / 同类探测 | Detect |
| `disableRasp` / 同类禁用 | DisableService |
| 内存马 inject/remove | MemshellService |
| `ping`（新） | 轻量存活，用于判断模块是否已 include |
| 未知 | `code=UNSUPPORTED` + nextHint |

实现阶段允许 **先 JSON 主路径，文本 fallback**，避免一次砍光旧 UI。

---

## 6. 管线语义（`action=pipeline`）

1. **Detect** — 环境 + RASP 指纹；可缓存于 Facade（手动「刷新诊断」失效）。
2. **Plan** — 输出有序策略 ID 列表（指纹只影响排序与跳过，不写死唯一路径）。
3. **Exec** — 依次执行直到 `ok` 或链尽。
4. **Verify** — 默认短命令带随机标记（如 `echo GSL_OK_<rand>` / 平台等价），输出必须含标记，防止空回显假成功。
5. **不自动** Disable / Memshell。

结果区固定轨迹摘要：

```text
[DETECT] … → [PLAN] s1>s2>s3 → [EXEC] hit=s2 → [VERIFY] ok
```

### 默认策略序（可按 Detect 重排）

| 序 | 策略 | 说明 |
|----|------|------|
| 1 | SoftDegrade | 清 SecurityManager / 常见上下文，为后续铺路 |
| 2 | JniNative | 仅当 native 已加载 |
| 3 | UnsafeForkAndExec | JDK8/11 常见 |
| 4 | ProcessImplDirect | 反射 ProcessImpl/UNIXProcess |
| 5 | ThreadContext / Finalize | 切调用栈 |
| 6 | TomcatJni | 容器 native 可用时 |
| 7 | ProcessBuilder / Runtime | 无 RASP 时最快基线 |

---

## 7. 客户端 UI

### Tab

| Tab | 职责 |
|-----|------|
| **诊断** | Detect + Plan 预览；复制报告 |
| **一键执行** | 默认页；`pipeline`；命令框；tried/evidence |
| **高级** | 禁用 RASP、内存马、JNI 加载/执行、路径工具；可强制 methodIndex |

### 交互

- 顶栏状态摘要：`RASP / JDK / 容器 / 推荐策略`。
- 输出优先展示 `output` + `evidence`；`raw` 折叠。
- 异步执行 + EDT 回写（保持现模式）。
- `NO_MODULE`：明确提示运行 `compile_rasp_bypass.bat`（或 sh）后再打包。

### 加载

```
open RaspBypassModule.classs
  → 可选 ASM morph（见下）
  → payload.include("RaspBypassModule", bytes)
  → ping / opsEnvironment 探测是否已缓存
```

---

## 8. ASM 多态与 JNI 约束

**允许**：private junk 字段、非 native 方法入口 NOP/无害指令、不影响描述符的次要变形。

**禁止**：

- 修改入口类 FQN（须保持 `RaspBypassModule` 或与 JNI 一致的约定名）
- 修改 `native` 方法名与描述符
- 修改 Facade 对外 `evalFunc` 方法名（兼容期）

与 `native/rasp_bypass_jni.c` 头部注释一致：客户端不得随机改名导致 `UnsatisfiedLinkError`。

---

## 9. JNI（路线 A 内，非独立 agent）

- `JniBridge`：按 OS/arch 选择 `rasp_bypass_win_x64.dll` / `linux_x64.so` 等 → 临时文件 → `System.load` → `jniExec`。
- Windows：保留 CreateProcess 直跑优先、失败再回退 popen 的策略。
- Plan 仅在库已加载或用户显式「加载 JNI」后提高 `JniNative` 优先级。
- **不做** 独立 attach 进程卸 agent（属路线 C）。

---

## 10. 构建与产物

```
compile_rasp_bypass.bat / .sh
  javac -source 1.8 -target 1.8
  → 生成 assets/RaspBypassModule.classs（及依赖 class 资源约定）
native 编译脚本 → assets 或文档化旁路投递
打 jar 时必须包含最新 .classs
```

文档约定：**改目标端源码后必须 compile 再打 jar**，否则 UI 新、远端旧。

---

## 11. 错误码与 UI

| code | 含义 | UI |
|------|------|-----|
| OK | 成功 | 展示 output / verify |
| BLOCKED | 策略链用尽或明确拦截 | tried + 建议 JNI/禁用 |
| NO_MODULE | 未 include / 资源缺失 | 编译与打包指引 |
| UNSUPPORTED | 环境不支持 | 说明 JDK/容器限制 |
| ERROR | 异常 | error 摘要 + raw |

---

## 12. 迁移节奏

1. **协议 + Facade + Detect/Plan/Exec 骨架** + `ping`
2. **从旧 `RaspBypassModule` 迁 strategies**（先搬后删巨类）
3. **UI 三 Tab + JSON 解析**（文本 fallback）
4. **Disable / Memshell 迁入高级**
5. **编译脚本、.classs、发版对齐**；清理或降级半成品 Utils 入口

### 明确本轮不做

- 独立 attachable native agent
- 将 MCP 注册为载荷插件
- 未授权对真实第三方 RASP 商业环境的攻击性验证
- 大规模重写 Godzilla `include`/`evalFunc` 框架本身

---

## 13. 验收标准

1. 无 RASP 环境：pipeline 成功，Verify 标记匹配。
2. 模拟策略失败：自动落到下一策略，`tried`/`evidence` 完整。
3. 删除 `.classs`：`NO_MODULE` 与编译提示。
4. 旧参数 `execCommand` + `cmd`/`methodIndex`/`autoDetect` 仍可用。
5. 有 JNI 库时：加载后 exec 有回显。
6. ASM morph 后：入口类名与 native 链接仍可用。
7. 源码无 2400 行单类承载全部技巧（巨类删除或缩成薄委托）。

---

## 14. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 多 class include 失败 | MVP 优先单入口 + 同包依赖明确上传顺序 |
| JSON 在旧载荷编码下损坏 | 选用 ASCII 友好字段；失败 fallback 文本 |
| 厂商 disable 误伤业务 | 不进自动 pipeline；高级二次确认文案 |
| JDK 17+ 强封装 | 策略标 UNSUPPORTED；优先 JNI / 已打开模块路径的路径 |
| 编译产物遗漏 | compile 脚本退出码 + 发版 checklist |

---

## 15. 参考路径（当前仓库）

- 客户端：`src/shells/plugins/java/RaspBypass.java`
- 目标端：`src/shells/plugins/java/assets/RaspBypassModule.java`
- Utils：`src/shells/plugins/java/assets/RaspBypassUtils.java`
- JNI：`native/rasp_bypass_jni.c`
- 编译：`compile_rasp_bypass.bat` / `.sh`（根或脚本目录，实现时对齐实际路径）
