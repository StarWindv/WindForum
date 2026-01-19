### API路由总览

| 路由路径                  | 方法   | 入参类型        | 返回值类型               | 返回格式        | 注意事项                                     |
|-----------------------|------|-------------|---------------------|-------------|------------------------------------------|
| `/api/register`       | POST | UserDTO     | Values              | -           | 需包含username和email；失败返回401                |
| `/api/verify`         | POST | UserDTO     | -                   | -           | 需包含email, username, verifyCode, codeHash |
| `/api/login`          | POST | UserDTO     | Map<String, String> | JSON        | 成功返回Session-ID；失败返回401                   |
| `/api/posts/upload`   | POST | PostDTO     | Values              | -           | 需通过Session-ID验证；空内容返回400                 |
| `/api/posts/comments` | POST | -           | -                   | -           | 未实现(TODO)                                |
| `/api/posts/get`      | POST | GetPostsDTO | Values              | JSON/JSON数组 | 支持三种查询方式:from/to, limit, post_id         |
| `/api/posts/getuser`  | POST | PostDTO     | Values              | JSON数组      | 需包含userEmail；失败返回404                     |
| `/index`              | GET  | -           | String              | HTML        | 返回index.html                             |
| `/dashboard`          | GET  | -           | String              | HTML        | 需通过Session-ID验证                          |
| `/editor`             | GET  | -           | String              | HTML        | 需通过Session-ID验证                          |
| `/login`              | GET  | -           | String              | HTML        | 返回login.html                             |
| `favicon.ico`         | GET  | -           | byte[]              | PNG         | 返回head.png                               |
| `robots.txt`          | GET  | -           | String              | text/plain  | 返回robots.txt内容                           |

### 详细说明

#### 1. 认证相关路由

**`/api/register`** (POST)
- 入参:UserDTO
  ```java
  {
    username: String,
    email: String
  }
  ```
- 返回: 400/200
- 注意: 在register后用户会收到一个邮箱验证码, 必须填写此邮箱验证码并在/api/verify验证结束后才算完整的注册流程

**`/api/verify`** (POST)
- 入参:UserDTO
  ```java
  {
    email: String,
    username: String,
    verifyCode: String,
    codeHash: String
  }
  ```
- 返回: 400/200

**`/api/login`** (POST)
- 入参:UserDTO
  ```java
  {
    email: String,
    codeHash: String
  }
  ```
- 返回:JSON对象
  ```json
  {
    "Session-ID": "string",
    "status": "boolean",
    "message": "string"
  }
  ```
- 注意:成功时返回Session-ID，失败返回401

#### 2. 帖子相关路由

**`/api/posts/upload`** (POST)
- 入参:PostDTO
  ```java
  {
    userEmail: String,
    title: String,
    content: String
  }
  ```
- 返回: 200
- 注意:
    - 需通过Session-ID验证（受保护路径）
    - 空内容返回400状态码
    - 调用`PostsTool.addPost()`

**`/api/posts/get`** (POST)
- 入参:GetPostsDTO
  ```java
  {
    from: int,
    to: int,
    limit: int,
    post_id: String,
    isArc: boolean
  }
  ```
- 返回:JSON对象或JSON数组
- 注意:
    - 三种查询方式互斥，只能使用一种
    - from/to必须同时提供或都不提供
    - 失败返回 400/404 状态码

**`/api/posts/getuser`** (POST)
获取用户的全部帖子
- 入参:PostDTO
  ```java
  {
    userEmail: String
  }
  ```
- 返回:JSON数组 
  ```java
  [{
    "post_id": post_id, 
    "email_str": email_str, 
    "title": title, 
    "content": content, 
    "status": status, 
    "create_time": create_time, 
    "last_update_time": last_update_time
  }, ...]
  ```
- 注意:失败返回 404 状态码

#### 3. 页面路由

**`/index`** (GET)
- 返回:HTML页面
- 注意:返回index.html

**`/dashboard`** (GET)
- 返回:HTML页面
- 注意:
    - 需通过Session-ID验证（受保护路径）
    - 返回dashboard.html

**`/editor`** (GET)
- 返回:HTML页面
- 注意:
    - 需通过Session-ID验证（受保护路径）
    - 返回editor.html

**`/login`** (GET)
- 返回:HTML页面
- 注意:返回login.html

#### 4. 静态资源路由

**`favicon.ico`** (GET)
- 返回:PNG图片
- 注意:返回head.png

**`robots.txt`** (GET)
- 返回:文本文件
- 注意:返回robots.txt内容

### 受保护路径列表

以下路径需要Session-ID验证:
- `/dashboard`
- `/api/posts/upload`
- `/api/posts/comments`
- `/editor`
未通过验证返回401状态码.

### Notes:
- 访问页面路由时如果加了末尾的后缀名会导致错误
- 只有当你访问静态资源时才需要加后缀名
- 访问静态资源时的前缀名必须是`/static/`, 不能是`./static/`
