// noinspection JSUnusedGlobalSymbols

/**
 * 所有API均大小写敏感
 * 
 * @param Upload 
 * 上传帖子的接口, 必须提供 Session-ID
 * 上传数据: {"email": user_email, "title": post's_Title, "content": post's_Content}
 * 请求头需要: {"Session-ID": session_id}
 * 上传数据必须都非空
 * 返回值: 200 {"status", true, "message": message}
 * 返回值: 400 {"status", false, "message": message}
 * 
 * @param comments
 * 上传评论的接口, 必须提供 Session-ID
 * 尚未实现
 * 
 * @param getPosts
 * 复合型 POST 接口, 必须提供 Session-ID
 * 上传数据:
 *  - 方法一:
 *      {"from": int, "to": int, "isArc": boolean}
 *      范围查询模式, 后端以创建日期进行 正序/倒序 排序后给出符合范围要求的帖子
 *  - 方法二:
 *      {"limit": int, "isArc": boolean}
 *      数量限制查询模式, 后端以创建日期进行 正序/倒序 排序后给出符合数量要求的前 `limit` 个帖子
 *  - 方法三:
 *      {"post_id": int}
 *      精准查询模式, 后端返回 ID 对应的帖子
 *  注意: 三种方法互斥, 如果混合了查询参数会报错, 返回 400
 *  返回值:
 *  404 - 无对应帖子
 *  400 - 参数错误
 *  500 - 查询时服务器内部错误
 *  200 - 正常通过
 *  正常通过时的数据:
 *  [{
 *      "post_id": post_id, 
 *      "email_str": email_str, 
 *      "title": title, 
 *      "content": content, 
 *      "status": status, 
 *      "create_time": create_time, 
 *      "last_update_time": last_update_time
 *  }, ...]
 *  当使用精准查询模式时, 返回值仍然是 Json 数组, 但内部仅有一个 Json 对象, 其余格式无差异
 * 
 * @param getUserPosts
 * 单用户全部帖子获取接口, 不需要 Session-ID
 * 上传数据: {"userEmail": userEmail}
 * 返回值:
 * 500 - 查询时服务器内部错误, 无其它值
 * 404 - 未找到该用户帖子, 无其它值
 * 200 - 正常通过, 返回 Json 数组
 *  [{
 *      "post_id": post_id, 
 *      "email_str": email_str, 
 *      "title": title, 
 *      "content": content, 
 *      "status": status, 
 *      "create_time": create_time, 
 *      "last_update_time": last_update_time
 *  }, ...]
 *
 * @param getUserVerboseInfo
 * 获取用户个人数据的接口, 必须要 Session-ID
 * 上传数据: 用户名 或 用户邮箱
 * {"username": username}
 * 或者
 * {"email"   : email}
 * 返回值:
 * 404: 未找到对应用户
 * 500: 服务器内部错误
 * 400: 参数错误
 * 200: 正常
 * 正常数据:
 * [{
 *     "user_id": user_id,
 *     "user_name": user_name,
 *     "email_str": email_str,
 *     "register_ip": register_ip,
 *     "reg_time": reg_time,
 *     "last_log_ip": last_log_ip,
 *     "last_log_time": last_log_time,
 *     "permission": permission,
 *     "status": status
 * }]
 * 备注:
 *  - 权限
 *    SuperAdmin(1, "SuperAdmin")
 *    Admin (2, "Admin")
 *    Normal(3, "Normal")
 *
 *  - 状态
 *    Active  = 1
 *    Deleted =-1
 *    Ban     = 0
 * 所有时间都是时间戳, 比如`1768690858654`
 */

const Upload="/api/posts/upload";
const comments="/api/posts/comments";
const getPosts="/api/posts/get";
const getUserPosts="/api/posts/getUserPosts";
const getUserVerboseInfo="/api/userinfo";


class PostDTO {
    constructor(email, title, content, channel_id) {
        if (!email || !email.trim()) {
        throw new Error('The email cannot be empty');
        }
        if (!title || !title.trim()) {
        throw new Error('The post\'s title cannot be empty');
        }
        if (!content || !content.trim()) {
        throw new Error('The post\s content cannot be empty');
        }
        if (!channel_id || !channel_id.trim()) {
        throw new Error('The post\s channel_id cannot be empty');
        }
        
        this.email = email;
        this.title = title;
        this.content = content;
        this.channel_id = channel_id;
    }
    
    static create(email, title, content, channel_id) {
        return new PostDTO(email, title, content, channel_id);
    }
}


/**
 * 帖子管理类
 * 负责帖子的上传、查询、获取单用户帖子等操作
 */
class PostManager {
  constructor(authorizer) {
    // 依赖注入Authorizer实例, 用于获取Session-ID和用户邮箱
    this.authorizer = authorizer || new Authorizer();
    this.SESSION_ID_HEADER = 'Session-ID';
  }

  /**
   * 上传帖子
   * @param {PostDTO|{email: string, title: string, content: string}} postData 帖子数据
   * @returns {Promise<{status: boolean, message: string}>} 上传结果
   * @throws {Error} 无Session-ID、参数错误、接口异常时抛出错误
   */
  async uploadPost(postData) {
    try {
      // 1. 获取并校验Session-ID
      const sessionId = this.authorizer.getSessionId();
      if (!sessionId) {
        throw new Error('未登录, 无有效Session-ID, 无法上传帖子');
      }

      // 2. 统一处理帖子数据（支持直接传对象或PostDTO实例）
      let post;
      if (postData instanceof PostDTO) {
        post = postData;
      } else {
        post = PostDTO.create(postData.email, postData.title, postData.content);
      }

      // 3. 发起上传请求
      const response = await fetch(Upload, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [this.SESSION_ID_HEADER]: sessionId // 请求头携带Session-ID
        },
        body: JSON.stringify({
          userEmail: post.email,
          title: post.title,
          content: post.content,
            channel_id: post.channel_id
        })
      });

      // 4. 处理响应结果
      const responseData = await response.json().catch(() => ({}));
      
      if (response.status === 400) {
        throw new Error(`帖子上传失败: ${responseData.message || '内容为空或参数错误'}`);
      }

      if (response.status === 200) {
        return {
          status: true,
          message: responseData.message || '帖子上传成功'
        };
      }

      throw new Error(`帖子上传请求异常, 状态码: ${response.status}`);
    } catch (error) {
      console.error('上传帖子出错:', error);
      throw error;
    }
  }

  /**
   * @param {Object} queryParams 查询参数（三种方式二选一）
   * @param {number} [queryParams.from] 范围查询-起始值
   * @param {number} [queryParams.to] 范围查询-结束值
   * @param {boolean} [queryParams.isArc] 排序方式（正序/倒序）
   * @param {number} [queryParams.limit] 数量限制查询-数量
   * @param {number} [queryParams.post_id] 精准查询-帖子ID
   * @returns {Promise<Array>} 帖子列表（精准查询返回长度为1的数组）
   * @throws {Error} 参数混合、无Session-ID、接口异常时抛出错误
   */
  async getPosts(queryParams) {
    try {
      // 1. 获取并校验Session-ID
      const sessionId = this.authorizer.getSessionId();
      if (!sessionId) {
        throw new UnAuthorizeError('未登录, 无有效Session-ID, 无法查询帖子');
      }

      // 2. 校验查询参数（三种方式互斥）
      const hasRangeParams = 'from' in queryParams && 'to' in queryParams && 'isArc' in queryParams;
      const hasLimitParams = 'limit' in queryParams && 'isArc' in queryParams;
      const hasIdParams = 'post_id' in queryParams;
      
      const validParamCount = [hasRangeParams, hasLimitParams, hasIdParams].filter(Boolean).length;
      
      if (validParamCount !== 1) {
        throw new Error('查询参数错误: 仅支持范围查询/数量限制查询/精准查询中的一种, 不可混合参数');
      }

      // 3. 发起查询请求
      const response = await fetch(getPosts, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [this.SESSION_ID_HEADER]: sessionId
        },
        body: JSON.stringify(queryParams)
      });

      // 4. 处理响应结果
      if (response.status === 400) {
        throw new Error('查询参数错误: 参数格式错误或混合了多种查询方式');
      }

      if (response.status === 404) {
        return []; // 无对应帖子返回空数组
      }

      if (response.status === 500) {
        throw new Error('服务器内部错误, 查询帖子失败');
      }

      if (response.status === 200) {
          const posts = await response.json();
          return posts.values;
      }

      throw new Error(`查询帖子请求异常, 状态码: ${response.status}`);
    } catch (error) {
      console.error('查询帖子出错:', error);
      throw error;
    }
  }

  /**
   * 获取指定用户的所有帖子（无需Session-ID）
   * @param {string} userEmail 目标用户邮箱
   * @returns {Promise<Array>} 该用户的帖子列表
   * @throws {Error} 邮箱为空、服务器异常时抛出错误
   */
  async getUserPosts(userEmail) {
    try {
      if (!userEmail.trim()) {
        throw new Error('Email cannot be empty');
      }

      const response = await fetch(getUserPosts, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userEmail })
      });

      if (response.status === 404) {
        return [];
      }

      if (response.status === 500) {
        throw new Error('500');
      }

      if (response.status === 200) {
        return await response.json();
      }

      throw new Error(`Failed get user posts: ${response.status}`);
    } catch (error) {
      console.error('获取用户帖子出错:', error);
      throw error;
    }
  }

  async getUserVerboseInfo(userEmail) {

  }

  /**
   * 上传评论（TODO: 未实现接口的占位方法）
   * @param {Object} commentData 评论数据（预留）
   * @returns {Promise<{status: boolean, message: string}>} 提示接口未实现
   */
  async uploadComment(commentData) {
    console.warn('评论接口尚未实现');
    return {
      status: false,
      message: '评论功能尚未实现, 请等待后续更新'
    };
  }
}
