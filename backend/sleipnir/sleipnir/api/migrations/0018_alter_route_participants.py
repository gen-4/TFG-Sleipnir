# Generated by Django 4.0.3 on 2022-05-17 16:21

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0017_alter_horse_image_alter_route_creator_and_more'),
    ]

    operations = [
        migrations.AlterField(
            model_name='route',
            name='participants',
            field=models.ManyToManyField(related_name='rider_route', to='api.horse'),
        ),
    ]
