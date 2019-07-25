from locust import HttpLocust, TaskSet, task
class UserTasks(TaskSet):
    @task
    def index(self):
        self.client.get("/news.php")
class WebsiteUser(HttpLocust):
    task_set = UserTasks