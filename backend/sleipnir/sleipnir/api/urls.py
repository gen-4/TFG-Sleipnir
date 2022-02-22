from django.contrib import admin
from django.urls import path, include

import sleipnir.api.views as views

api_urls = [
    path('user/', include([
        path('signup', views.signup, name='signup'),
        path('signin', views.signin, name='signin'),
    ])),
]