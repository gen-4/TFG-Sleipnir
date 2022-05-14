from enum import unique
from django.db import models

from django.contrib.auth.models import User

# Create your models here.

class Rider(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    is_admin = models.BooleanField(null=False, default=False)
    last_x_coord = models.FloatField(null=True, default=None)
    last_y_coord = models.FloatField(null=True, default=None)
    observers = models.ManyToManyField('self', symmetrical=False)

    def __str__(self):
        return self.user.username+'('+self.id.__str__()+')'



class Route(models.Model):
    creator = models.ForeignKey(Rider, null=False, default=0, on_delete=models.CASCADE)
    route_name = models.CharField(max_length=128, unique=True, null=False)
    max_participants = models.DecimalField(default=50, null=False, max_digits=2, decimal_places=0)
    current_participants = models.DecimalField(default=1, null=False, max_digits=2, decimal_places=0)
    duration = models.DecimalField(null=False, max_digits=4, decimal_places=0)
    celebration_date = models.DateTimeField(null=False)
    participants = models.ManyToManyField(to=Rider, symmetrical=False, related_name='rider_route')

    def __str__(self):
        return self.route_name+' <- '+self.creator.__str__()+' || '+self.celebration_date.__str__()

class Message(models.Model):
    writer = models.ForeignKey(Rider, null=False, default=0, on_delete=models.CASCADE)
    route = models.ForeignKey(Route, null=False, default=0, on_delete=models.CASCADE, related_name='route_message')
    message = models.TextField(null=False)
    date = models.DateTimeField(null=False, auto_now=True)

    def __str__(self):
        return self.route.__str__()+' '+self.writer.user.username+': '+self.message+' ('+self.date.__str__()+')'


class Record(models.Model):
    rider = models.ForeignKey(Rider, null=False, default=0, on_delete=models.CASCADE)
    record_name = models.CharField(max_length=128, null=False)
    distance = models.FloatField(null=False)
    duration = models.IntegerField(null=False, default=0)
    avg_speed = models.FloatField(null=False, default=0)
    date = models.DateField(null=False, auto_now=True)

    def __str__(self):
        return self.record_name+' <- '+self.rider.__str__()+' || '+self.date.__str__()


class Point(models.Model):
    x_coord = models.FloatField(null=False)
    y_coord = models.FloatField(null=False)
    position = models.DecimalField(null=False, default=0, max_digits=2, decimal_places=0)
    route = models.ForeignKey(Route, null=True, on_delete=models.CASCADE, related_name='route_point')
    record = models.ForeignKey(Record, null=True, on_delete=models.CASCADE, related_name='record_point')

    def __str__(self):
        refered = self.route
        if not refered:
            refered = self.record
        return refered.__str__()+' -> '+self.x_coord.__str__()+' : '+self.y_coord.__str__()