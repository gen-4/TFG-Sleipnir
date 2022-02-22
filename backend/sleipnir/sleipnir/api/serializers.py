from logging.config import valid_ident
from rest_framework import serializers
from rest_framework.validators import UniqueValidator
from django.contrib.auth.models import User

from .models import Rider

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
                raise serializers.ValidationError("Wrong telegram user. It musts start by \'@\'")
        return value