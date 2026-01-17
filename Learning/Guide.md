### 返回值

#### 要求
需要返回值的常用业务函数需要使用使用`Values`类进行包装

#### 格式
```Java
Values.from(
    boolean,
    message,
    data
);
```
当不需要`message`时, 需要使用空字符串进行占位操作
