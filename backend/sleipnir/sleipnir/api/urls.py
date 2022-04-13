from django.contrib import admin
from django.urls import path, include

import sleipnir.api.views as views

api_urls = [
    path('user/', include([
        path('signup', views.signup, name='signup'),
        path('login', views.login, name='login'),
        path('<id>', views.getRider, name='get_rider'),
    ])),
    path('route/', include([
        path('create_route', views.createRoute, name='create_route'),
    ])),
]