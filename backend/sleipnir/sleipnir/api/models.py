from enum import unique
from django.db import models

from django.contrib.auth.models import User

# Create your models here.

class Rider(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    telegram_user = models.CharField(max_length=16, unique=True, null=True)
    is_admin = models.BooleanField(null=False, default=False)

    def __str__(self):
        return self.user.username+'('+self.id.__str__()+')'

class Route(models.Model):
    creator = models.ForeignKey(Rider, null=False, default=0, on_delete=models.CASCADE)
    route_name = models.CharField(max_length=128, unique=True, null=False)
    max_participants = models.DecimalField(default=50, null=False, max_digits=2, decimal_places=0)
    current_participants = models.DecimalField(default=1, null=False, max_digits=2, decimal_places=0)
    duration = models.DecimalField(null=False, max_digits=4, decimal_places=0)
    celebration_date = models.DateTimeField(null=False)
    participants = models.ManyToManyField(to=Rider, related_name='rider_route')

    def __str__(self):
        return self.route_name+' <- '+self.creator.__str__()+' || '+self.celebration_date.__str__()

class Point(models.Model):
    x_coord = models.FloatField(null=False)
    y_coord = models.FloatField(null=False)
    position = models.DecimalField(null=False, default=0, max_digits=2, decimal_places=0)
    route = models.ForeignKey(Route, null=False, default=0, on_delete=models.CASCADE, related_name='route_point')

    def __str__(self):
        return self.route.__str__()+' -> '+self.x_coord.__str__()+' : '+self.y_coord.__str__()