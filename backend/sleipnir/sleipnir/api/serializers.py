from logging.config import valid_ident
from rest_framework import serializers
from django.contrib.auth.models import User, Group

from .models import Rider

class UserSigninSerializer(serializers.Serializer):
    username = serializers.CharField(required = True)
    password = serializers.CharField(required = True)


class GroupSerializer(serializers.ModelSerializer):

    class Meta:
        model = Group
        fields = ['name']

class UserSerializer(serializers.ModelSerializer):
    groups = GroupSerializer(source='groups', many=True)

    class Meta:
        model = User
        fields = ['username', 'first_name', 'last_name', 'email', 'groups']

class RiderSerializer(serializers.ModelSerializer):
    user = UserSerializer(source='user')

    class Meta:
        model = Rider
        fields = ['user', 'telegram_user']

class RiderSignupSerializer(serializers.ModelSerializer):

    def validate_telegram_user(self, value):
        if not value.startswith('@'):
            raise serializers.ValidationError("Wrong telegram user. It musts start by \'@\'")
        return value

    class Meta:
        model = Rider
        fields = ['user', 'telegram_user']
        depth=1

    def create(self, validated_data):
        user_data = validated_data.pop('user')
        rider = Rider.objects.create(**validated_data)
        User.objects.create(rider=rider, **user_data)
        return rider