# Generated by Django 4.0.3 on 2022-04-21 12:59

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0009_alter_point_route_record'),
    ]

    operations = [
        migrations.AddField(
            model_name='point',
            name='record',
            field=models.ForeignKey(null=True, on_delete=django.db.models.deletion.CASCADE, related_name='record_point', to='api.record'),
        ),
    ]
