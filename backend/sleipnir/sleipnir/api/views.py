import json
from sqlite3 import DatabaseError
from django.contrib.auth import authenticate
from django.db import transaction
from rest_framework.authtoken.models import Token
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.status import (
    HTTP_400_BAD_REQUEST,
    HTTP_404_NOT_FOUND,
    HTTP_200_OK,
    HTTP_500_INTERNAL_SERVER_ERROR,
)
from rest_framework.response import Response
from psycopg2 import IntegrityError

from django.contrib.auth.models import User
from simplejson import JSONDecoder, JSONEncoder

from .models import Rider, Route, Point
from .serializers import PointSerializer, RiderSignupSerializer, RouteSerializer, UserLoginSerializer, RiderSerializer

# Create your views here.

"""
    IMPORTANTE EL select_related CUANDO CLAVES FORANEAS
"""

@api_view(["POST"])
@permission_classes((AllowAny,))  # here we specify permission by default we set IsAuthenticated
def login(request):
    signin_serializer = UserLoginSerializer(data = request.data)
    if not signin_serializer.is_valid():
        return Response(signin_serializer.errors, status = HTTP_400_BAD_REQUEST)


    user = authenticate(
            username = signin_serializer.data['username'],
            password = signin_serializer.data['password'] 
        )
    if not user:
        return Response({'detail': 'Invalid Credentials or activate account'}, status=HTTP_404_NOT_FOUND)

    token, _ = Token.objects.get_or_create(user = user)
    
    rider = Rider.objects.get(user=user)
    user_serialized = RiderSerializer(rider)

    return Response({
        'token': token.key,
        'user': user_serialized.data
    }, status=HTTP_200_OK)

@api_view(["POST"])
@permission_classes((AllowAny,)) 
def signup(request):
    signup_serializer = RiderSignupSerializer(data = request.data)

    if not signup_serializer.is_valid():
        return Response(signup_serializer.errors, status = HTTP_400_BAD_REQUEST)

    data = signup_serializer.validated_data
    try:
        with transaction.atomic():
            user = User(**data['user'])
            user.set_password(data['user']['password'])
            user.save()
            rider = Rider(user=user, telegram_user=data['telegram_user'])
            rider.save()

    except IntegrityError:
        return Response({'detail': 'User already created'}, status = HTTP_400_BAD_REQUEST)

    rider_serializer = RiderSerializer(data=rider)

    return Response(rider_serializer.data, status=HTTP_200_OK)

@api_view(["GET"])
def getRider(request, id):

    try:
        rider = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'User not found'}, status = HTTP_404_NOT_FOUND)

    rider_serializer = RiderSerializer(rider)
    return Response(rider_serializer.data, status = HTTP_200_OK)


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
