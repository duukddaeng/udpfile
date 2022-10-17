# 基于UDP的多人文件传输功能实现

## 信息封装格式
发送方请求：

    requ+发送方用户名 （请求发送文件）

接收方应答：

    okok+接收方用户名 （同意接收文件）
    nono+接收方用户名 （拒绝接收文件）
    succ+接收方用户名 （成功接收文件）
    fail+接收方用户名 （接收文件失败）
    next+接收方用户名 （本组文件数据已接收）

文件发送：

    info+size（4byte）+filename
    data+offset（4byte）+filedata （1024byte）
    over+offset（4byte）+filedata （最后一组数据）

用户加入退出：

    join：port
    leav：port