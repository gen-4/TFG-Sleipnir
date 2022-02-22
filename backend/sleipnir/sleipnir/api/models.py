from django.db import models

from django.contrib.auth.models import User

# Create your models here.

class Rider(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    telegram_user = models.CharField(max_length=16, unique=True, null=True)
    is_admin = models.BooleanField(null=False, default=False)

    def __str__(self):
        return self.user.username