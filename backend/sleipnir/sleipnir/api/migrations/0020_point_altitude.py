# Generated by Django 4.0.3 on 2022-06-03 17:41

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0019_rider_friends_alter_rider_observers'),
    ]

    operations = [
        migrations.AddField(
            model_name='point',
            name='altitude',
            field=models.FloatField(default=0.0, null=True),
        ),
    ]
