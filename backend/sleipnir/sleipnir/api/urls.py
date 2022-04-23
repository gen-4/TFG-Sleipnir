from django.contrib import admin
from django.urls import path, include

import sleipnir.api.riderViews as riderViews
import sleipnir.api.routeViews as routeViews

api_urls = [
    path('user/', include([
        path('signup', riderViews.signup, name='signup'),
        path('login', riderViews.login, name='login'),
        path('<id>', riderViews.getRider, name='get_rider'),
    ])),
    path('route/', include([
        path('create_route', routeViews.createRoute, name='create_route'),
        path('get_routes', routeViews.getRoutes, name='get_routes'),
        path('<routeId>/join_route', routeViews.joinRoute, name='join_route'),
        path('<routeId>/leave_route', routeViews.leaveRoute, name='leave_route'),
        path('<routeId>/has_joined/<userId>', routeViews.hasJoined, name='has_route'),
        path('register_route_data', routeViews.registerRouteData, name='register_route_data'),
        path('rider_records/<riderId>', routeViews.getRiderRecords, name='get_rider_records'),
        path('detailed_record/<recordId>', routeViews.getDetailedRecord, name='get_detailed_record'),
    ])),
]