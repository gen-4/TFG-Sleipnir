from django.contrib import admin

from .models import Rider, Route, Point

# Register your models here.

admin.site.register(Rider)
admin.site.register(Route)
admin.site.register(Point)