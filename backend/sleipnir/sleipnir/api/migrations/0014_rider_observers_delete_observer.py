# Generated by Django 4.0.3 on 2022-05-13 16:24

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0013_remove_rider_telegram_user_rider_last_x_coord_and_more'),
    ]

    operations = [
        migrations.AddField(
            model_name='rider',
            name='observers',
            field=models.ManyToManyField(to='api.rider'),
        ),
        migrations.DeleteModel(
            name='Observer',
        ),
    ]
