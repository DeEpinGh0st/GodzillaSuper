# GSL5 — Godzilla Super Loader 5

##BUG反馈群



<img width="676" height="841" alt="image" src="https://github.com/user-attachments/assets/69ee0f77-f286-4cc3-a670-04d2fc221f80" />

## 更新日志

### 3.1.3（2026-07-24）
- **MCP Bearer Token 鉴权**：`/sse` `/message` `/health` `/config` 全路由鉴权，非本机绑定无 Token 拒绝启动
- **CLI 自动写配置**：`java -jar gsl5.jar mcp` 启动后自动写入 Claude Code + Codex 配置（含 Authorization header）
- **CLI 显式 Token**：命令行直接打印完整 Bearer Token，无需去文件里找
- **Linux headless 修复**：无 GUI 环境不再报 Font NPE

### 3.1.2（2026-07-17）
- RASP 绕过全链路：Detect→Plan→Exec→Verify 管线
- OpenRASP 深软关、命令回显修复、Shell 加载遮罩竞态修复
- JNI 自动加载、Win x64 DLL 内置

### 3.1.1（2026-07-17）
- PHP 免杀模板 0KB 修复

### 3.1.0（2026-07-15）
- NetCore 动态载荷 + 全局 MCP Claude/Codex 配置

---




基于 Godzilla 深度二次开发的 **WebShell 管理与红队后渗透平台**。在保留 Godzilla 全部能力的基础上，扩展了 **MCP 服务（AI 驱动）**、**团队协作（多数据源）**、**RASP 绕过 + 字节码多态混淆**、**内存马注入** 等能力，并提供完整的操作审计。

> ⚠️ 本工具仅供**授权的安全测试与研究**使用，严禁用于任何非法用途。使用本工具产生的任何后果由使用者自行承担。详见文末[免责声明](#免责声明).

---

## 目录

- [核心特性](#核心特性)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [使用指南](#使用指南)
- [MCP 服务（AI 操控）](#mcp-服务ai-操控)
- [配置文件](#配置文件)
- [编译与构建](#编译与构建)
- [常见问题](#常见问题)
- [免责声明](#免责声明)

---

## 核心特性

### 1. 双运行模式
- **GUI 模式（默认）**：图形化操作界面，启动时可选择数据源（见下表）。
- **MCP 无头模式**：`java -jar gsl5.jar mcp [port] [bindHost]`，内置 HTTP/SSE 服务，供 **Claude Desktop / Claude Code / Codex** 等 AI 助手远程调用全部功能。默认 **绑定 `0.0.0.0:9123`（全网卡）**，可指定网卡 IP / `127.0.0.1`。

### 2. 数据源与团队协作
启动时由 `StartupModeDialog` 选择数据源：

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| **单机模式** | 本地 SQLite `data.db` | 个人使用 |
| **团队-远程 SQLite** | UNC 共享路径（如 `\\host\share\data.db`） | 小团队、内网共享 |
| **团队-PostgreSQL** | 远程 PG，多人共享 Shell 列表 + 操作审计 | 多人协作 |

- `core/Db.java`：SQLite 持久化层（`shell` / `shellEnv` / `plugin` / `seting` / `shellGroup` 五张表）
- `core/MigrateDb.java`：SQLite → PostgreSQL 一键迁移工具（`java -cp gsl5.jar;lib/postgresql-*.jar core.MigrateDb [host] [port] [db] [user] [pass]`）

### 3. Shell 全功能管理
集中管理 **JSP / ASPX / PHP / ASP / ASP.NET Core** 多类 WebShell：

- **文件管理**（`ShellFileManager`）：浏览目录、上传/下载、删除/复制/移动、改属性、大文件分片续传、从 URL 远程下载到目标
- **命令执行**（`NewCmd`）：Windows 走 `cmd /c`、Linux 走 `bash`，支持交互终端
- **数据库工具**：MySQL / Oracle / SQL Server / PostgreSQL / SQLite，执行 SQL、保存常用连接
- **批量连接测试、搜索、克隆、导入导出**（`gsl5://import?data=...` 链接共享配置）
- **NetCore 载荷**（`NetCoreDynamicPayload` + `NETCORE_AES_BASE64`）：面向 ASP.NET Core Middleware 的完整动态载荷（文件 / 命令 / SQL / 插件加载）

### 4. RASP 绕过与字节码多态混淆（RaspBypass 插件）
- **命令执行绕过**：`Unsafe.allocateInstance` + `forkAndExec`、JNI 原生执行、新线程 / GC finalize、Tomcat-JNI、`ProcessImpl` 直调、反射关闭后执行
- **RASP 探测与禁用**：自动识别 OpenRASP / JRASP / Elkeid 等，支持禁用 Hook、改配置、卸载 Agent
- **ASM 多态混淆**（`RaspBypass.obfuscateModule()`）：上传 `RaspBypassModule` 前，用 ObjectWeb ASM 注入**随机字段名 + 随机 NOP 指令**，使每次字节码特征不同，规避哈希 / 静态签名检测
- **内存马注入**：Tomcat（Filter / Servlet / Listener）、Spring（Controller）、Jetty、**VM Anonymous Class**（隐蔽性最高）

### 5. 插件集

| 插件 | 平台 | 功能 |
|------|------|------|
| `TH_TOOLS` | Java / C# | Potato 系列提权（EfsPotato / BadPotato / GodPotato / SweetPotato）、Shellcode 注入、自定义 Payload |
| `Mimikatz` | 通用 | 凭据抓取 |
| `Useradd` | Java / C# / 通用 | 添加账号 |
| `OaTools` | Java / C# | 金蝶 / 致远 / 泛微 / 用友 / Weblogic / vCenter 等专项代理 |
| `ShellAvscan` | 通用 | 目标安全软件探测 |
| `RaspBypass` | Java | RASP 绕过 + 内存马（见上） |
| `McpService` | 通用（应用级） | 内置 MCP HTTP/SSE 服务；全局菜单入口，支持 Claude / Codex 一键写配置 |
| NetCore 插件组 | .NET Core | RealCmd / PortScan / Zip / HttpProxy / EasySocks / EvalCode / ExecuteAssembly / ShellcodeLoader |

### 6. 现代化 UI 与审计
- FlatLaf 主题、壁纸管理器、透明度调节
- `OperationAuditLog` 全量操作审计：记录**谁、何时、做了什么**，团队模式下可经 `oplog_query` 查询

---

## 项目结构

```
gsl/
├── src/
│   ├── core/                  # 核心框架
│   │   ├── ApplicationContext.java     # 全局上下文（含 godMode 等）
│   │   ├── Db.java                     # SQLite 持久化层
│   │   ├── MigrateDb.java              # SQLite → PostgreSQL 迁移
│   │   ├── OperationAuditLog.java      # 操作审计
│   │   ├── shell/                      # ShellEntity 等
│   │   ├── shellprocessor/             # ASP/PHP/JSP/C# 编码与变形处理器
│   │   ├── c2profile/                  # C2 配置与内存马模板（含 .jar/.javax）
│   │   └── ui/                         # MainActivity、StartupModeDialog、
│   │                                   # ShellFileManager、DataView、WallpaperManager 等
│   ├── shells/
│   │   ├── payloads/           # 各平台动态载荷（Java / C# / NetCore 模块）
│   │   │   └── netcore/        # NetCoreShell + payload_core.dll
│   │   ├── cryptions/          # 流量加密（JavaAes / JavaC2、csharpAes、phpXor、aspXor、NETCORE_AES_BASE64）
│   │   │   └── netcore/        # Middleware 模板 + AES Base64 加密器
│   │   └── plugins/            # 增强插件（java / csharp / netcore / generic）
│   │       ├── generic/McpService.java # MCP HTTP/SSE 服务（AI 操控入口，应用级）
│   │       └── netcore/        # NetCore 插件 UI + assets/*.dll
│   ├── util/                   # HTTP、IP 库、工具函数
│   └── data/                   # 内置资源（av.json、META-INF）
├── native/                     # RASP 绕过 JNI 源码（rasp_bypass_jni.c）+ build_jni 脚本
├── bin/gsl5.jar                # 预编译主程序
├── KeyGen.java                 # 授权文件生成器（输出 license.lic）
├── compile_rasp_bypass.bat/.sh # RASP 模块编译脚本
└── build_all_rasp_bypass.bat   # 一键全量编译
```

> 运行/构建还需外部依赖（`lib/`：ASM、PostgreSQL 驱动等；`bin/`：okhttp、kotlin-stdlib 等），未纳入本仓库。

---

## 快速开始

### 0. 环境要求
- **Java**：JDK / JRE 8 或以上（开发机已验证 `1.8.0_431`）
- **授权文件**：`license.lic` 必须存在于运行目录

### 1. 生成授权
编译并运行 `KeyGen.java`，生成 `license.lic` 放到运行目录（可设置有效期、绑定目录）：
```bash
javac KeyGen.java && java KeyGen
```
授权格式 `GSL1:<AES-CBC 密文>`，内含 `notBefore | notAfter | 绑定目录 SHA256` 并用 HMAC-SHA256 签名。

### 2. 启动 GUI
```bash
java -jar bin/gsl5.jar
```
启动后选择数据源（单机 SQLite / 远程 SQLite / PostgreSQL），进入主界面。

### 3. 启动 MCP 无头模式（AI 操控）
```bash
java -jar bin/gsl5.jar mcp                 # 默认 0.0.0.0:9123
java -jar bin/gsl5.jar mcp 9999            # 自定义端口（仍绑 0.0.0.0）
java -jar bin/gsl5.jar mcp 9999 127.0.0.1  # 端口 + 绑定地址
java -jar bin/gsl5.jar mcp 0.0.0.0:9123    # host:port 写法
java -jar bin/gsl5.jar mcp 192.168.1.10:9123
```
然后在 Claude / Codex 的 MCP 配置中添加（详见 [MCP 服务](#mcp-服务ai-操控)）。

### 4. 下载预编译 Release
- **3.1.3（最新）**：MCP Token 鉴权 + CLI 自动写配置 + Linux headless 修复 — [Release](https://github.com/Xaaaa-bip/GodzillaSuper/releases/tag/3.1.3) · [jar](https://github.com/Xaaaa-bip/GodzillaSuper/releases/download/3.1.3/gsl5.jar)
- **3.1.2**：RASP 绕过全链路 — [Release](https://github.com/Xaaaa-bip/GodzillaSuper/releases/tag/3.1.2) · [jar](https://github.com/Xaaaa-bip/GodzillaSuper/releases/download/3.1.2/gsl5.jar)
- **3.1.1**：PHP 免杀 0KB 修复 — [Release](https://github.com/Xaaaa-bip/GodzillaSuper/releases/tag/3.1.1) · [jar](https://github.com/Xaaaa-bip/GodzillaSuper/releases/download/3.1.1/gsl5.jar)
- **3.1.0**：NetCore + 全局 MCP — [Release](https://github.com/Xaaaa-bip/GodzillaSuper/releases/tag/3.1.0) · [jar](https://github.com/Xaaaa-bip/GodzillaSuper/releases/download/3.1.0/gsl5.jar)

---

## 使用指南

> 以下为 **GUI 模式**的日常操作流程。AI / MCP 操控见下一节 [MCP 服务](#mcp-服务ai-操控)。

### 添加与连接 Shell

主界面左侧为 **Shell 分组树**，右侧为操作区。菜单 / 右键分组 → **添加 Shell**，填写配置（字段对应 `data.db` 的 `shell` 表）：

| 字段 | 说明 |
|------|------|
| URL | WebShell 地址 |
| 密码 / 密钥（secretKey） | 通信密钥 |
| Payload | `JavaDynamicPayload` / `CSharpPayload` / `PhpPayload` / `NetCoreDynamicPayload` 等，需与目标服务端匹配 |
| 加密（cryption） | `JAVA_AES_BASE64` / `JAVA_C2` / `PHP_XOR` / `CSHARP_AES_BASE64` / `NETCORE_AES_BASE64` 等 |
| 编码（encoding） | 目标控制台编码。MCP 连接时自动从 DB 带入；为空/`auto` 时用 `chcp`/`locale` 自动检测并写回（Windows 多为 GBK，Linux 多为 UTF-8）；也可 `shell_detect_encoding` |
| 请求头 / 左右标志（reqLeft / reqRight） | 自定义请求体包裹方式 |
| 代理 / 超时 | `proxyType/Host/Port`、`connTimeout`、`readTimeout` |
| 备注 / 笔记 | `remark`、`note` |

保存后双击或右键 **连接**；支持 **测试连接**、**批量测试**、**搜索**、**克隆**、**导入 / 导出**（`gsl5://import?data=...` 链接共享配置）。

### 主要操作标签

选中已连接的 Shell 后，右侧切换标签：

- **NewCmd** —— 命令执行。Windows 走 `cmd /c`、Linux 走 `bash`。
- **ShellFileManager** —— 文件管理：浏览、上传 / 下载、删除、复制、移动、改属性、大文件分片续传、从 URL 远程下载到目标。
- **数据库** —— 配置目标库（MySQL / Oracle / SQL Server / PostgreSQL / SQLite）→ 执行 SQL、管理连接配置。
- **插件** —— 右键 Shell → 插件 → 选择功能模块。

### RaspBypass 插件流程

1. **智能诊断**（`opsEnvironment`）：探测目标 OS、进程、已注入 RASP / Agent 指纹，给出绕过建议。
2. **命令执行**：选绕过方法 ——
   - `Unsafe.allocateInstance + forkAndExec`（绕构造函数监控）
   - **JNI 原生执行**（脱离 JVM 监控层，需先加载 native 库）
   - 新线程 / GC finalize（切割调用栈，破坏上下文检测）
   - Tomcat-JNI / `ProcessImpl` 直调 / 反射关闭后执行
3. **RASP Disable**：识别 OpenRASP / JRASP / Elkeid 等 → 禁用 Hook / 改配置 / 卸载 Agent。
4. **Memory Shell**：注入 Tomcat（Filter / Servlet / Listener）/ Spring（Controller）/ Jetty / VM Anonymous Class 内存马。

> `RaspBypassModule` 上传前自动经 ASM 多态混淆（随机字段名 + 随机 NOP），规避哈希 / 签名检测。

### 提权与后渗透插件

- **TH_TOOLS** —— Potato 系列提权（EfsPotato / BadPotato / GodPotato / SweetPotato）、Shellcode 注入、自定义 Payload
- **Mimikatz** 凭据抓取 ｜ **Useradd** 添加账号 ｜ **ShellAvscan** 探测目标安全软件
- **OaTools** —— 金蝶 / 致远 / 泛微 / 用友 / Weblogic / vCenter 等专项代理

### NetCore 载荷（ASP.NET Core）

面向 **ASP.NET Core Middleware** 的完整动态载荷（不是传统 aspx）：

| 类型 | 名称 |
|------|------|
| Payload | `NetCoreDynamicPayload` |
| Cryption | `NETCORE_AES_BASE64` |
| 入口 | ASP.NET Core Middleware（`.cs`） |

**能力**（`payload_core.dll` / 类名 `LY`）：会话 `test`/`close`、基础信息、文件管理、命令执行、SQL（目标进程需已加载对应 DbProviderFactory）、插件 `include` 加载。

**插件**（`src/shells/plugins/netcore/`）：RealCmd / SuperTerminal / PortScan / Zip / HttpProxy / EasySocksProxy / InlineExecuteAssembly / EvalCode / ShellcodeLoader（Windows）。未移植强依赖 System.Web / 域提权的 MemoryShell、Potato 等。

**使用步骤**：
1. 生成：Payload=`NetCoreDynamicPayload`，加密器=`NETCORE_AES_BASE64`
2. 将生成的 Middleware 放入 ASP.NET Core 项目：`app.UseMiddleware<GslCoreShellMiddleware>();`
3. 添加 Shell，密码密钥一致后连接
4. 打开 Shell 后插件页可见 NetCore 功能

源码说明见 `src/shells/cryptions/netcore/README.md`。

### 团队协作（PostgreSQL 模式）

启动选 "团队-PostgreSQL" → 多人共享同一 Shell 列表 → 所有操作自动写入审计日志（MCP 可经 `oplog_query` 查询；GUI 经操作审计面板查看）。从单机迁到团队库可用 `MigrateDb` 一键迁移。

---

## MCP 服务（AI 操控）

### 入口与绑定
- **GUI**：菜单 **配置 → MCP 服务**（若无配置菜单则 fallback 到插件菜单）。面板约 780×520，支持绑定地址 / 端口 / 启动 / 停止。
- **CLI**：`java -jar gsl5.jar mcp [port] [bindHost]`，也支持 `host:port` 写法。
- **默认绑定**：`0.0.0.0:9123`（全网卡）；可改为 `127.0.0.1` 或指定网卡 IP。
- 启动后日志类似：`[MCP] 服务已启动 bind=0.0.0.0:9123`，并提供可访问 URL 列表。

### 一键写入 Claude / Codex 配置
GUI 面板第二行：
| 按钮 | 写入目标 |
|------|----------|
| **写入全部配置** | Claude Code + Claude Desktop + Codex |
| **写入 Claude** | `~/.claude/mcp.json`、项目 `mcp.json` / `.mcp.json`、Claude Desktop `claude_desktop_config.json` |
| **写入 Codex** | `~/.codex/config.toml` → `[mcp_servers.gsl5]` |

也可经 MCP 工具 `mcp_config`：`client=claude|codex|all`，`write=true`，可选 `host` / `outputPath`。

### Claude 配置示例
```json
{
  "mcpServers": {
    "gsl5": {
      "type": "sse",
      "url": "http://127.0.0.1:9123/sse"
    }
  }
}
```

### Codex 配置示例
```toml
[mcp_servers.gsl5]
type = "sse"
url = "http://127.0.0.1:9123/sse"
```

### Encoding 自检
连接 / 执行时自动处理远程控制台编码：
1. 显式 `encoding` 参数 → 强制使用
2. `auto` 或 DB 为空 → 探测（`chcp` / `locale`）并写回 DB
3. DB 已有值 → 使用 DB 值，并做冷连接校验
4. 缓存命中不重复探测（`auto` / `shell_detect_encoding`）

可用工具 `shell_detect_encoding` 主动探测并写回。

### 可用工具（以源码 `McpService.java` 为准）

| 分类 | 工具 | 说明 |
|------|------|------|
| **Shell 管理** | `shell_list` | 列出所有 Shell（可按分组过滤） |
| | `shell_get` | 获取单个 Shell 完整配置 |
| | `shell_add` / `shell_edit` / `shell_delete` | 增 / 改 / 删 Shell |
| | `shell_clone` | 克隆 Shell |
| | `shell_backup` | 备份 Shell 列表 |
| **查询 / 批量** | `shell_count` | 按分组统计 |
| | `shell_search` | 关键词搜索 |
| | `shell_batch_test` | 批量测试连接 |
| **远程探测** | `shell_info` | 远程系统信息（OS / 用户 / 目录） |
| | `shell_test` | 测试单条连接 |
| | `shell_detect_encoding` | 探测远程控制台编码并写回 DB |
| | `process_list` | 列出进程 |
| | `net_info` | 网络连接信息 |
| **命令执行** | `shell_exec` | 执行系统命令（`encoding` 支持 GBK/UTF-8/`auto`） |
| **文件操作** | `file_list` | 列目录 |
| | `file_read` | 读文件（自动判断文本 / 二进制） |
| | `file_search` / `file_roots` | 按名搜索 / 列文件系统根目录 |
| | `file_upload_local` / `file_download_local` | 本机路径直传 / 直存（不经 AI 中转） |
| | `file_delete` / `file_copy` / `file_move` | 删 / 复制 / 移动 |
| | `file_mkdir` / `file_attr` | 建目录 / 设属性（权限 / 时间戳） |
| | `file_remote_down` | 从 URL 远程下载到目标 |
| **数据库** | `db_exec` | 执行 SQL |
| | `db_list_types` / `db_configs` | 列支持的库类型 / 管理连接配置 |
| **Payload / 生成** | `payload_list` | 列出所有载荷与加密器（含 NetCore） |
| | `shell_create` | 生成 Shell 文件（可指定 `cryption`、`genFile`、`c2Profile`、`obfuscation`） |
| | `c2profile_list` / `c2profile_get` | 列出 / 获取 C2 配置 |
| **环境** | `shell_env` | 读取 / 设置 Shell 环境变量 |
| **导入导出** | `shell_export` / `shell_import` | 导出 / 导入（`gsl5://` 链接） |
| **设置** | `settings_get` / `settings_set` | 读 / 改应用设置 |
| **配置** | `config_read` / `config_write` | 读 / 写 `config.yaml` |
| **MCP 管理** | `mcp_status` | 服务状态（绑定地址 / 可访问 URL） |
| | `mcp_config` | 生成 / 写入 Claude 或 Codex 配置（`client`/`write`/`host`） |
| **审计** | `oplog_query` | 查询团队操作日志 |

> `shell_create` 的 `obfuscation` 参数默认 `default`（不启用增强）；传其它值会写入 `godMode` 设置，启用增强传输模式。C2 模板可通过 `c2profile_list` 查询，指定 `c2Profile` 参数可避免弹 UI 选择框。

### 典型工作流
```
1. 启动 GSL5 MCP 服务（GUI 面板或 mcp 无头）
2. 写入 Claude / Codex 配置并连接 SSE
3. shell_list / shell_detect_encoding → 查看 Shell 并校正编码
4. shell_exec          → 在目标执行命令
5. file_list/file_read → 浏览、读取文件
6. db_exec             → 查询数据库
7. oplog_query         → 回溯操作审计
```

---

## 配置文件

运行时读取/写入的文件（位于运行目录，非源码仓库内容）：

| 文件 | 说明 |
|------|------|
| `license.lic` | 授权文件，格式 `GSL1:<AES-CBC 密文>`，由 `KeyGen.java` 生成，可绑定目录 + 有效期 |
| `data.db` | SQLite 数据库，存储 Shell 配置 / 插件 / 环境变量 / 设置 / 分组 |
| `config.yaml` | 运行配置（MCP 端口、认证、团队 PG 连接等） |

`config.yaml` 结构示例（**请替换为你的真实连接信息**）：
```yaml
server:
  port: 9123              # MCP HTTP 服务端口
  auth:
    enable: false         # 是否启用 MCP 认证
    password: ""
mcp:
  enable: true            # 是否启动 MCP 服务
database:                 # 团队 PostgreSQL（仅团队模式使用）
  host: 127.0.0.1
  port: 5432
  database: gsl5
  username: gsl5
  password: <your-password>
```

---

## 编译与构建

> ⚠️ 源码默认 **GBK** 编码（`McpService.java` 为 UTF-8）。编辑 GBK 文件时避免用会重编码为 UTF-8 的工具，纯 ASCII 改动建议用 `sed`。

```bash
# 1) 编译核心源码（GBK）
javac -encoding GBK -cp "lib/*;bin/*" -d out/production/gsl5 \
  src/core/**/*.java src/shells/**/*.java src/util/**/*.java

# 2) 编译 MCP 服务（UTF-8，依赖已编译的核心）
javac -encoding UTF-8 -cp "lib/*;bin/*;out/production/gsl5" \
  -d out/production/gsl5 src/shells/plugins/generic/McpService.java

# 3) 编译 RASP 服务端模块
compile_rasp_bypass.bat          # Windows
./compile_rasp_bypass.sh         # Linux/macOS

# 4)（可选）编译 JNI 库 —— 自定义原生绕过
cd native && build_jni.bat win-x64    # 或 build_jni.sh linux-x64

# 5) 打包
cd out/production/gsl5 && jar cf gsl5.jar *
```

**更新已有 jar 中的单个 class**（免重新打包）：
```bash
jar uf out/artifacts/gsl5_jar/gsl5.jar shells/plugins/generic/McpService.class
```

构建产物输出到 `out/production/gsl5/`（class）与 `out/artifacts/gsl5_jar/gsl5.jar`（jar）。`lib/`（ASM、PostgreSQL 等）与 `bin/`（okhttp、kotlin-stdlib 等）依赖需自行准备。

---

## 常见问题

**Q：启动报 `License invalid`？**
A：确保 `license.lic` 在运行目录且未过期；用 `KeyGen.java` 重新生成。

**Q：MCP 连接失败？**
A：检查绑定地址与端口（默认 `0.0.0.0:9123`）是否被占用、防火墙是否放行；确认 Claude / Codex 配置的 URL 与实际可访问地址一致。本机客户端可用 `127.0.0.1`，跨机需写网卡 IP 并用 `mcp_config`/`写入配置` 生成对应 URL。

**Q：Shell 连接超时？**
A：检查目标 URL 是否可达，Payload / 加密方式是否与目标服务端匹配。

**Q：中文乱码？**
A：目标输出默认 GBK（Windows）/ UTF-8（Linux），可在 Shell 配置中设置 `encoding`，或用 MCP `shell_detect_encoding` / `encoding=auto` 自动探测写回。

**Q：NetCore 载荷怎么用？**
A：生成时选 `NetCoreDynamicPayload` + `NETCORE_AES_BASE64`，把 Middleware `.cs` 挂进 ASP.NET Core 管道（`UseMiddleware`），不是上传 aspx。纯文件上传默认不能当 aspx 用。

**Q：团队模式连不上数据库？**
A：PostgreSQL 需允许远程连接（`pg_hba.conf`），确认用户名密码正确；先用 `StartupModeDialog` 的 Test Connection 验证。

**Q：如何绕过目标上的 RASP？**
A：用 `RaspBypass` 插件，先"自动探测"识别 RASP 类型，再选对应绕过方法（JNI 原生执行 / Unsafe+forkAndExec 等）；模块上传自带 ASM 多态混淆。

---

## 免责声明

本工具仅供**安全研究和授权测试**使用，严禁用于任何非法用途。使用本工具产生的任何后果由使用者自行承担。所有操作会被审计日志完整记录。
