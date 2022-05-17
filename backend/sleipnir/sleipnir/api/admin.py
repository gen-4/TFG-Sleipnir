from django.contrib import admin

from .models import Horse, Rider, Route, Point, Record, Message

# Register your models here.

admin.site.register(Rider)
admin.site.register(Route)
admin.site.register(Point)
admin.site.register(Record)
admin.site.register(Message)
admin.site.register(Horse)