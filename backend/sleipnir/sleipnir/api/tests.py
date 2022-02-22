from django.test import TestCase

import json

from .models import Rider 
from django.contrib.auth.models import User

# Create your tests here.

class LoginTestCase(TestCase):
    
    def setUp(self):
        user = User(username="a", first_name="a", last_name="a", email="a")
        user.set_password("a")
        user.save()
        rider = Rider(user=user, telegram_user="@a")
        rider.save()

    def test_login(self):
        data = {
            "username" : "a",
            "password" : "a"
        }

        json = json.dumps(data)

        response = self.client.post('/user/login',json)

        self.assertIsNotNone(response.json['token'])
