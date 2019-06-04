from locust import HttpLocust, TaskSet
import random

def vote(l):
    opts = ["a", "b"]
    l.client.cookies.clear()
    l.client.post("/", {"vote": random.choice(opts)})

class UserBehavior(TaskSet):
    tasks = {vote: 1}

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 5000
    max_wait = 9000
