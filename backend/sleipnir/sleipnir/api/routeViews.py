import json
from datetime import datetime

from django.db import transaction
from rest_framework.decorators import api_view
from rest_framework.status import (
    HTTP_400_BAD_REQUEST,
    HTTP_404_NOT_FOUND,
    HTTP_200_OK,
    HTTP_500_INTERNAL_SERVER_ERROR,
    HTTP_403_FORBIDDEN
)
from rest_framework.response import Response
from psycopg2 import IntegrityError

from .models import Route, Point, Rider, Record, Message, Horse
from .serializers import PointSerializer, RouteSerializer, GetRoutesSerializer
from .serializers import RecordPointSerializer, RecordSerializer, RecordsSerializer
from .serializers import GetRecordSerializer, MessageSerializer, PostMessageSerializer
from .serializers import HorseParticipantsSerializer


@api_view(['POST'])
def createRoute(request):
    json_data = request.data
    points_array = json_data.pop('points')
    horseId = json_data.pop('horse')
    
    horse = Horse.objects.get(pk=horseId)
    

    route_serializer = RouteSerializer(data=json_data)
    
    route = None

    with transaction.atomic():
        try:
            if route_serializer.is_valid():
                route = Route(**route_serializer.validated_data)
                route.save()    
                route.participants.add(horse)
                route.save()
            
            
        
        except IntegrityError:
            return Response({'detail': 'Route creation failed'}, status=HTTP_400_BAD_REQUEST)

   
        json_points = json.loads(points_array)

        for point in json_points:
            point['route'] = route.id
            point_serializer = PointSerializer(data = point)
            
            try:
                if point_serializer.is_valid():
                    point = Point(**point_serializer.validated_data)
                    point.save()
                    
            except IntegrityError:
                return Response({'detail': 'Route creation failed: Unable to save a point'}, status=HTTP_500_INTERNAL_SERVER_ERROR)

    return Response(route_serializer.data, status=HTTP_200_OK)


@api_view(['GET'])
def getRoutes(request):
    routes_list = Route.objects.all()
    route_serializer = GetRoutesSerializer(routes_list, many=True)
    routes_list = route_serializer.data
    
    routes_list[:] = [route for route in routes_list if not (datetime.strptime(route['celebration_date'], '%Y-%m-%dT%H:%M:%S') <= datetime.now())]

    return Response(routes_list, status=HTTP_200_OK)


@api_view(['POST'])
def joinRoute(request, routeId):
    data = request.data
    horseId = data.pop('horse')
    horse = Horse.objects.get(pk=horseId)
    route = Route.objects.get(pk=routeId)

    if route.current_participants >= route.max_participants:
        return Response({'detail': 'Unable to join route: Max number of participants reached'}, status=HTTP_400_BAD_REQUEST)

    elif horse in route.participants.all():
        return Response({'detail': 'Unable to join route: Already joined'}, status=HTTP_400_BAD_REQUEST)

    route.current_participants += 1
    route.participants.add(horse)

    try:
        route.save()
        
    except IntegrityError:
        return Response({'detail': 'Unable to join route'}, status=HTTP_500_INTERNAL_SERVER_ERROR)

    route_serializer = RouteSerializer(route)

    return Response(route_serializer.data, status=HTTP_200_OK)


@api_view(['POST'])
def leaveRoute(request, routeId):
    data = request.data

    rider = Rider.objects.get(pk=data['user'])
    route = Route.objects.get(pk=routeId)

    route.current_participants -= 1

    horses = route.participants.all()
    rider_horses = Horse.objects.filter(owner=rider)
    for horse in rider_horses:
        if horse in horses:
            route.participants.remove(horse) 

    try:
        route.save()
    
    except IntegrityError:
        return Response({'detail': 'Unable to leave route'}, status=HTTP_500_INTERNAL_SERVER_ERROR)

    route_serializer = RouteSerializer(route)

    return Response(route_serializer.data, status=HTTP_200_OK)


@api_view(['GET'])
def hasJoined(request, routeId, userId):

    rider = Rider.objects.get(pk=userId)
    route = Route.objects.get(pk=routeId)

    if rider == route.creator:
        return Response({'joined': 0}, HTTP_200_OK)

    else:
        horses = route.participants.all()
        rider_horses = Horse.objects.filter(owner=rider)
        for horse in rider_horses:
            if horse in horses:
                return Response({'joined': 1}, HTTP_200_OK)

            
    return Response({'joined': -1}, HTTP_200_OK)

@api_view(['POST'])
def registerRouteData(request):
    json_data = request.data
    points_array = json_data.pop('points')

    record_serializer = RecordSerializer(data = json_data)
    record_data = {}

    with transaction.atomic():
        try:
            if record_serializer.is_valid():
                record_data = record_serializer.validated_data
            
            record = Record(**record_data)
            record.save()
        
        except IntegrityError:
            return Response({'detail': 'Record registration failed'}, status=HTTP_400_BAD_REQUEST)

        
        record_serializer = RecordSerializer(record)
        record_id = record_serializer.data['id']

        
        for point in points_array:
            point['record'] = record_id
            point_serializer = RecordPointSerializer(data = point)
            
            data = {}

            try:
                if point_serializer.is_valid():
                    data = point_serializer.validated_data
                    
                point = Point(**data)
                point.save()
            except IntegrityError:
                return Response({'detail': 'Record registration failed: Unable to save a point'}, status=HTTP_500_INTERNAL_SERVER_ERROR)

    return Response(record_serializer.data, status=HTTP_200_OK)

@api_view(['GET'])
def getRiderRecords(request, riderId):
    rider = Rider.objects.get(pk=riderId)
    records = Record.objects.filter(rider=rider)

    record_serializer = RecordsSerializer(records, many=True)

    return Response(record_serializer.data, status=HTTP_200_OK)

@api_view(['GET'])
def getDetailedRecord(request, recordId):
    record = Record.objects.get(pk=recordId)

    record_serializer = GetRecordSerializer(record)

    return Response(record_serializer.data, status=HTTP_200_OK)

@api_view(['GET'])
def getMessages(request, routeId):
    route = Route.objects.get(pk=routeId)

    messages = Message.objects.filter(route=route)

    message_serializer = MessageSerializer(messages, many=True)

    return Response(message_serializer.data, status=HTTP_200_OK)

@api_view(['POST'])
def postMessage(request, routeId):
    route = Route.objects.get(pk=routeId)

    message_serializer = PostMessageSerializer(data=request.data)

    if message_serializer.is_valid():
        message = Message(**message_serializer.validated_data)
        message.route = route

        message.save()
    
    message_serializer = MessageSerializer(message)
    return Response(message_serializer.data, status=HTTP_200_OK)


@api_view(['GET'])
def getParticipants(request, routeId):
    route = Route.objects.get(pk=routeId)

    horse_serializer = HorseParticipantsSerializer(route.participants, many=True)
    horses = horse_serializer.data
    for horse in horses:
        try:
            horse['image'] = horse['image'].replace('/horse_images/', '')
        except:
            pass

    return Response(horse_serializer.data, status=HTTP_200_OK)


@api_view(['GET'])
def getPastRoutes(request):
    routes_list = Route.objects.all()
    route_serializer = GetRoutesSerializer(routes_list, many=True)
    routes_list = route_serializer.data

    routes_list[:] = [route for route in routes_list if not (datetime.strptime(route['celebration_date'], '%Y-%m-%dT%H:%M:%S') >= datetime.now())]

    return Response(routes_list, status=HTTP_200_OK)


@api_view(['POST'])
def updateRoute(request, routeId):
    data = request.data
    horseId = data.pop('horse')
    horse = Horse.objects.get(pk=horseId)
    route = Route.objects.get(pk=routeId)

    if route.celebration_date >= datetime.now():
        return Response({'detail': 'Forbidden, route still in progress'}, status=HTTP_403_FORBIDDEN)

    route.current_participants = 1
    route.participants.set([horse])
    try:
        route.celebration_date = datetime.strptime(data['celebration_date'], '%Y-%m-%dT%H:%M:%S')
    except ValueError:
        route.celebration_date = datetime.strptime(data['celebration_date'], '%Y-%m-%dT%H:%M')
    route.max_participants = data['max_participants']

    try:
        route.save()
        
    except IntegrityError:
        return Response({'detail': 'Unable to join route'}, status=HTTP_500_INTERNAL_SERVER_ERROR)

    route_serializer = RouteSerializer(route)

    return Response(route_serializer.data, status=HTTP_200_OK)