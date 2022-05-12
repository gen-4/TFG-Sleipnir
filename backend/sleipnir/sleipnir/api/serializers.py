from logging.config import valid_ident
from rest_framework import serializers
from rest_framework.validators import UniqueValidator
from django.contrib.auth.models import User

from .models import Message, Rider, Route, Point, Record, Observer

class UserLoginSerializer(serializers.Serializer):
    username = serializers.CharField(required = True)
    password = serializers.CharField(required = True)

class UserSerializer(serializers.ModelSerializer):

    class Meta:
        model = User
        fields = ['id', 'username', 'first_name', 'last_name', 'email']

class RiderSerializer(serializers.ModelSerializer):
    user = UserSerializer()

    class Meta:
        model = Rider
        fields = ['id', 'user', 'telegram_user']

class UserSignup(serializers.ModelSerializer):

    class Meta:
        model = User
        fields = ['username', 'first_name', 'last_name', 'password', 'email']

class RiderSignupSerializer(serializers.Serializer):
    user = UserSignup()
    telegram_user = serializers.CharField(
        max_length=16, 
        validators=[UniqueValidator(queryset=Rider.objects.all())],
        required=True,
        allow_null=True
    )

    def validate_telegram_user(self, value):
        if value:
            if not value.startswith('@'):
                raise serializers.ValidationError("Wrong telegram user. It musts start with \'@\'")
        return value




class PointSerializer(serializers.ModelSerializer):

    class Meta:
        model = Point
        fields = ['id', 'x_coord', 'y_coord', 'position', 'route']

class RouteSerializer(serializers.ModelSerializer):

    class Meta:
        model = Route
        fields = ['id', 'creator', 'route_name', 'max_participants', 'current_participants', 'duration', 'celebration_date']



class GetPointsSerializer(serializers.ModelSerializer):

    class Meta:
        model = Point
        fields = ['x_coord', 'y_coord', 'position']

class GetRoutesSerializer(serializers.ModelSerializer):
    points = GetPointsSerializer(source='route_point', many=True, read_only=True)
    
    class Meta:
        model = Route
        fields = ['id', 'creator', 'route_name', 'max_participants', 'current_participants', 'duration', 'celebration_date', 'points']


class RecordPointSerializer(serializers.ModelSerializer):

    class Meta:
        model = Point
        fields = ['id', 'x_coord', 'y_coord', 'position', 'record']

class RecordSerializer(serializers.ModelSerializer):

    class Meta:
        model = Record
        fields = ['id', 'rider', 'record_name', 'distance', 'duration', 'avg_speed']

class GetRecordSerializer(serializers.ModelSerializer):
    points = GetPointsSerializer(source='record_point', many=True, read_only=True)

    class Meta:
        model = Record
        fields = ['id', 'record_name', 'distance', 'duration', 'avg_speed', 'date', 'points']

class RecordsSerializer(serializers.ModelSerializer):

    class Meta:
        model = Record
        fields = ['id', 'record_name', 'date']

class MessageUserSerializer(serializers.ModelSerializer):

    class Meta:
        model = User
        fields = ['username']

class MessageRiderSerializer(serializers.ModelSerializer):
    user = MessageUserSerializer()

    class Meta:
        model = Rider
        fields = ['id', 'user']

class MessageSerializer(serializers.ModelSerializer):
    writer = MessageRiderSerializer()

    class Meta:
        model = Message
        fields = ['writer', 'message', 'date']

class PostMessageSerializer(serializers.ModelSerializer):

    class Meta:
        model = Message
        fields = ['writer', 'message']

class ObserverSerializer(serializers.ModelSerializer):

    class Meta:
        model = Observer
        fields = ['id', 'telegram_user']

class AddObserverSerializer(serializers.ModelSerializer):

    class Meta:
        model = Observer
        fields = ['telegram_user']
    
    def validate_telegram_user(self, value):
        if value:
            if not value.startswith('@'):
                raise serializers.ValidationError("Wrong telegram user. It musts start with \'@\'")
        return value