from django.contrib import admin

from .models import Rider, Route, Point, Record

# Register your models here.

admin.site.register(Rider)
admin.site.register(Route)
admin.site.register(Point)
admin.site.register(Record)