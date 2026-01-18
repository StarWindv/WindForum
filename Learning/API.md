
| 路由路径                  | HTTP方法 | 请求参数                                                             | 返回值/响应                                           | 说明                    |
|-----------------------|--------|------------------------------------------------------------------|--------------------------------------------------|-----------------------|
| `/`                   | GET    | 无                                                                | "Server is Running"                              | 服务器状态检查               |
| `/index`              | GET    | 无                                                                | HTML页面                                           | 返回index.html          |
| `/dashboard`          | GET    | 无                                                                | HTML页面                                           | 返回dashboard.html(需认证) |
| `/favicon.ico`        | GET    | 无                                                                | 图片数据                                             | 返回网站图标                |
| `/api/register`       | POST   | UserDTO对象<br>- username<br>- email                               | 401状态码或成功                                        | 发送注册验证码               |
| `/api/verify`         | POST   | UserDTO对象<br>- email<br>- username<br>- verifyCode<br>- codeHash | 验证结果                                             | 验证注册验证码               |
| `/api/login`          | POST   | UserDTO对象<br>- email<br>- codeHash                               | JSON对象:<br>- Session-ID<br>- status<br>- message | 用户登录                  |
| `/api/posts/upload`   | POST   | PostDTO对象                                                        | 400状态码或成功                                        | 上传帖子(需认证)             |
| `/api/posts/comments` | POST   | 未实现                                                              | 未实现                                              | 评论功能(待开发)             |
| `/api/posts/get`      | POST   | GetPostsDTO对象<br>- from/to<br>- limit<br>- post_id<br>- isArc    | JSON数组或404状态码                                    | 获取帖子列表或单个帖子           |
| `/static/*`           | GET    | 无                                                                | 静态文件                                             | 获取静态资源                |

### 关键说明: 

1. **认证路由**(需Session-ID头部): 
    - `/dashboard`
    - `/api/posts/upload`
    - `/api/posts/comments`

2. **参数说明**: 
    - `UserDTO`: 包含用户相关字段(username, email, verifyCode, codeHash等)
    - `PostDTO`: 包含帖子相关字段 (userEmail, title, content)
    - `GetPostsDTO`: 包含查询参数((((from, to), isArc), (limit, isArc)), post_id )
    - 对于获取帖子的API, 会返回 `[{post_id, email_str, title, content, status, create_time, last_update_time}]`这样的json数组
   注意, 前端只需要定义同样格式的json即可

3. **特殊路由**: 
    - `/api/posts/get` 支持三种查询方式(from/to范围、limit限制数量、post_id单个查询)，但只能使用其中一种
    - 所有API路由返回JSON格式数据(除明确返回HTML或图片的路由)
    - 部分API返回json数组而不是单个json对象
    - `/static/*` 按照静态文件夹路径返回资源, 并伴有对应的 mimetype 和`nosniff`; 如 `/static/image/head.png` 会返回一张图片, 而`static/focus.js`会返回一个js文件

4. **错误处理**: 
    - 401: 未授权/认证失败
    - 400: 请求参数错误
    - 404: 资源未找到
