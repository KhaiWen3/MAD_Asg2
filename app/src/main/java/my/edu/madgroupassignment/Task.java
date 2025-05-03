package my.edu.madgroupassignment;

public class Task {
    public String title, description, notificationTime;

    // 必须的空构造函数（Firebase 反序列化用）
    public Task() {
    }

    // 正确的构造函数：初始化所有字段
    public Task(String title, String description, String notificationTime) {
        this.title = title;
        this.description = description;
        this.notificationTime = notificationTime;
    }
}
