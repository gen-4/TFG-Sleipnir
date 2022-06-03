from django.contrib import admin
from django.urls import path, include

import sleipnir.api.riderViews as riderViews
import sleipnir.api.routeViews as routeViews

api_urls = [
    path('user/', include([
        path('signup', riderViews.signup, name='signup'),
        path('login', riderViews.login, name='login'),
        path('<id>', riderViews.getRider, name='get_rider'),

        path('<id>/observers', riderViews.getRiderObservers, name='get_rider_observers'),
        path('<id>/add_observer', riderViews.addObserver, name='add_observer'),
        path('<userId>/delete_observer/<id>', riderViews.deleteObserver, name='delete_observer'),
        path('<userId>/observeds', riderViews.getObservedsLastLocation, name='observeds_last_location'),
        path('<userId>/modify_last_location', riderViews.updateLastLocation, name='update_last_location'),

        path('<userId>/horses', riderViews.getRiderHorses, name='rider_horses'),
        path('<userId>/horse/<horseId>/delete', riderViews.deleteHorse, name='delete_horse'),
        path('<userId>/add_horse', riderViews.addHorse, name='add_horse'),

        path('<id>/friends', riderViews.getRiderFriends, name='get_rider_friends'),
        path('<id>/add_friend', riderViews.addFriend, name='add_friend'),
        path('<userId>/delete_friend/<id>', riderViews.deleteFriend, name='delete_friend'),
        path('<userId>/get_friend_routes', riderViews.getFriendRoutes, name='get_friend_routes'),
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

        path('<routeId>/messages', routeViews.getMessages, name='get_messages'),
        path('<routeId>/post_message', routeViews.postMessage, name='post_message'),

        path('<routeId>/participants', routeViews.getParticipants, name='get_participants'),

        path('get_historic', routeViews.getPastRoutes, name='get_past_routes'),
        path('<routeId>/update', routeViews.updateRoute, name='update_route'),
        path('<routeId>/detailed', routeViews.getDetailedRoute, name='get_route_detailed'),
    ])),
]