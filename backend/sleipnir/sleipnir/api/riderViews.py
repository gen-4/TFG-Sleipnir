from datetime import datetime

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
    HTTP_403_FORBIDDEN
)
from rest_framework.response import Response
from psycopg2 import IntegrityError
from django.conf import settings

from django.contrib.auth.models import User

from .models import Rider, Horse, Route
from .serializers import RiderSignupSerializer, UserLoginSerializer, RiderSerializer
from .serializers import ObserverSerializer, LastLocationSerializer, UpdateLastLocationSerializer
from .serializers import HorseSerializer, HorseAddSerializer, GetRoutesSerializer

# Create your views here.
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
    signup_serializer = RiderSignupSerializer(data=request.data)


    
    try:
        with transaction.atomic():
            if signup_serializer.is_valid():
                user = User(**signup_serializer.validated_data['user'])
                user.set_password(signup_serializer.validated_data['user']['password'])
            else:
                return Response(signup_serializer.errors, status = HTTP_400_BAD_REQUEST)

            user.save()
            rider = Rider(user=user)
            rider.save()

    except IntegrityError:
        return Response({'detail': 'User already created'}, status = HTTP_400_BAD_REQUEST)

    rider_serializer = RiderSerializer(rider)

    return Response(rider_serializer.data, status=HTTP_200_OK)

@api_view(["GET"])
def getRider(request, id):

    try:
        rider = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'User not found'}, status = HTTP_404_NOT_FOUND)

    rider_serializer = RiderSerializer(rider)
    return Response(rider_serializer.data, status = HTTP_200_OK)


@api_view(['GET'])
def getRiderObservers(request, id):

    try:
        rider = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'User not found'}, status = HTTP_404_NOT_FOUND)

    observers = rider.observers.all()
    observer_serializer = ObserverSerializer(observers, many=True)

    return Response(observer_serializer.data, status=HTTP_200_OK)

@api_view(['POST'])
def addObserver(request, id):

    try:
        rider = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'User not found'}, status = HTTP_404_NOT_FOUND)

    
    user = User.objects.get(username=request.data["username"])
    observer = Rider.objects.get(user=user)

    rider.observers.add(observer)
    rider.save()

    observer_serializer = ObserverSerializer(observer)
    return Response(observer_serializer.data, status=HTTP_200_OK)

@api_view(['POST'])
def deleteObserver(request, userId, id):

    try:
        rider = Rider.objects.get(pk=userId)
        observer = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    rider.observers.remove(observer)
    rider.save()

    return Response({'detail': f'Observer {id} deleted'}, status=HTTP_200_OK)

@api_view(['GET'])
def getObservedsLastLocation(request, userId):
    try:
        rider = Rider.objects.get(pk=userId)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    observeds = []
    riders = Rider.objects.all()
    for user in riders:
        if rider in user.observers.all():
            observeds.append(user)
    observeds_serializer = LastLocationSerializer(observeds, many=True)

    return Response(observeds_serializer.data, status=HTTP_200_OK)


@api_view(['POST'])
def updateLastLocation(request, userId):
    try:
        rider = Rider.objects.get(pk=userId)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    last_loc_serializer = UpdateLastLocationSerializer(data=request.data)
    if last_loc_serializer.is_valid():
        data = last_loc_serializer.validated_data
        rider.last_x_coord = data['last_x_coord']
        rider.last_y_coord = data['last_y_coord']
        rider.save()

    return Response({'detail': 'Last location updated'}, status=HTTP_200_OK)


@api_view(['GET'])
def getRiderHorses(request, userId):
    try:
        rider = Rider.objects.get(pk=userId)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    horses = Horse.objects.filter(owner=rider)
    horse_serializer = HorseSerializer(horses, many=True)
    horses_list = horse_serializer.data
    for horse in horses_list:
        try:
            horse['image'] = horse['image'].replace('/horse_images/', '')
        except:
            pass

    return Response(horses_list, status=HTTP_200_OK)


@api_view(['POST'])
def deleteHorse(request, userId, horseId):
    try:
        rider = Rider.objects.get(pk=userId)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    horse = Horse.objects.get(pk=horseId)
    if (rider != horse.owner):
        return Response({'detail': 'Forbidden action'}, status=HTTP_403_FORBIDDEN)

    horse.delete()

    return Response({'detail': f'Horse {horseId} deleted'}, status=HTTP_200_OK)


@api_view(['POST'])
def addHorse(request, userId):
    try:
        rider = Rider.objects.get(pk=userId)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    horse_serializer = HorseAddSerializer(data=request.data)

    horse = None
    if horse_serializer.is_valid():
        horse = Horse(**horse_serializer.validated_data)
        horse.owner = rider
    
    horse.save()

    horse_serializer = HorseSerializer(horse)

    return Response(horse_serializer.data, status=HTTP_200_OK)

@api_view(['POST'])
def addHorseImage(request, userId, horseId):

    try:
        rider = Rider.objects.get(pk=userId)
        horse = Horse.objects.get(pk=horseId)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    if (rider != horse.owner):
        return Response({'detail': 'Forbidden action'}, status=HTTP_403_FORBIDDEN)

    horse.image = request.data
    horse.save()

    return Response(status=HTTP_200_OK)



@api_view(['GET'])
def getRiderFriends(request, id):

    try:
        rider = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'User not found'}, status = HTTP_404_NOT_FOUND)

    friends = rider.friends.all()
    friend_serializer = ObserverSerializer(friends, many=True)

    return Response(friend_serializer.data, status=HTTP_200_OK)

@api_view(['POST'])
def addFriend(request, id):

    try:
        rider = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'User not found'}, status = HTTP_404_NOT_FOUND)

    
    user = User.objects.get(username=request.data["username"])
    friend = Rider.objects.get(user=user)
    
    rider.friends.add(friend)
    rider.save()

    friend_serializer = ObserverSerializer(friend)
    return Response(friend_serializer.data, status=HTTP_200_OK)

@api_view(['POST'])
def deleteFriend(request, userId, id):

    try:
        rider = Rider.objects.get(pk=userId)
        friend = Rider.objects.get(pk=id)
    
    except:
        return Response({'detail': 'Entity not found'}, status = HTTP_404_NOT_FOUND)

    rider.friends.remove(friend)
    rider.save()

    return Response({'detail': f'Observer {id} deleted'}, status=HTTP_200_OK)

@api_view(['GET'])
def getFriendRoutes(request, userId):
    routes_list = Route.objects.all()
    rider = Rider.objects.get(pk=userId)
    friends = rider.friends.all()
    final_route_list = []
    friends_in_routes = []

    for route in routes_list:
        if (route.celebration_date > datetime.now()):
            for friend in friends:
                friend_horses = Horse.objects.filter(owner=friend)
                if any(element in friend_horses for element in route.participants.all()):
                    friends_in_routes.append(friend.user.username)
                    final_route_list.append(route)

    route_serializer = GetRoutesSerializer(final_route_list, many=True)
    for i, route in enumerate(route_serializer.data):
        route['friend'] = friends_in_routes[i]

    return Response(route_serializer.data, status=HTTP_200_OK)