import json
from datetime import datetime
from django.db import transaction
from rest_framework.decorators import api_view
from rest_framework.status import (
    HTTP_400_BAD_REQUEST,
    HTTP_404_NOT_FOUND,
    HTTP_200_OK,
    HTTP_500_INTERNAL_SERVER_ERROR,
)
from rest_framework.response import Response
from psycopg2 import IntegrityError

from .models import Route, Point, Rider
from .serializers import PointSerializer, RouteSerializer, GetRoutesSerializer


@api_view(['POST'])
def createRoute(request):
    json_data = request.data
    points_array = json_data.pop('points')

    route_serializer = RouteSerializer(data = json_data)
    route_data = {}

    with transaction.atomic():
        try:
            if route_serializer.is_valid():
                route_data = route_serializer.validated_data
            
            route = Route(**route_data)
            route.participants.add(Rider.objects.get(pk=route.creator))
            route.save()
        
        except IntegrityError:
            return Response({'detail': 'Route creation failed'}, status=HTTP_400_BAD_REQUEST)

        
        route_serializer = RouteSerializer(route)
        route_id = route_serializer.data['id']

        points_array_dict = json.loads(points_array)
        for point in points_array_dict:
            point['route'] = route_id
            point_serializer = PointSerializer(data = point)
            
            data = {}

            try:
                if point_serializer.is_valid():
                    data = point_serializer.validated_data
                    
                point = Point(**data)
                point.save()
            except IntegrityError:
                return Response({'detail': 'Route creation failed: Unable to save a point'}, status=HTTP_500_INTERNAL_SERVER_ERROR)

    return Response(route_serializer.data, status=HTTP_200_OK)


@api_view(['GET'])
def getRoutes(request):
    routes_list = Route.objects.all()
    route_serializer = GetRoutesSerializer(routes_list, many=True)
    routes_list = route_serializer.data
    
    for route in routes_list:
        
        if datetime.strptime(route['celebration_date'], '%Y-%m-%dT%H:%M:%S') <= datetime.now():
            routes_list.remove(route)

    return Response(routes_list, status=HTTP_200_OK)


@api_view(['POST'])
def joinRoute(request):
    data = request.data
    print(data)
    rider = Rider.objects.get(pk=data['user'])
    route = Route.objects.get(pk=data['route'])

    if route.current_participants >= route.max_participants:
        return Response({'detail': 'Unable to join route: Max number of participants reached'}, status=HTTP_400_BAD_REQUEST)

    elif rider in route.participants.all():
        return Response({'detail': 'Unable to join route: Already joined'}, status=HTTP_400_BAD_REQUEST)

    route.current_participants += 1
    route.participants.add(rider)

    try:
        route.save()
        
    except IntegrityError:
        return Response({'detail': 'Unable to join route'}, status=HTTP_500_INTERNAL_SERVER_ERROR)

    route_serializer = RouteSerializer(route)

    return Response(route_serializer.data, status=HTTP_200_OK)