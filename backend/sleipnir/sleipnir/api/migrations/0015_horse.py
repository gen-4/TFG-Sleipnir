# Generated by Django 4.0.3 on 2022-05-16 14:29

from django.db import migrations, models
import django.db.models.deletion
import sleipnir.api.models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0014_rider_observers_delete_observer'),
    ]

    operations = [
        migrations.CreateModel(
            name='Horse',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=128, unique=True)),
                ('height', models.FloatField()),
                ('weight', models.FloatField()),
                ('coat', models.IntegerField(choices=[(0, 'White'), (1, 'Black'), (2, 'Brown'), (3, 'Sorrel'), (4, 'Trush'), (5, 'Appaloosa'), (6, 'Overo'), (7, 'Roan'), (8, 'Bay'), (9, 'Elizabethan')])),
                ('breed', models.CharField(choices=[('Andaluz (PRE)', 'Andaluz (PRE)'), ('Pura Raza Inglesa', 'Pura Raza Inglesa'), ('Pura Raza Galega', 'Pura Raza Galega'), ('Árabe', 'Árabe'), ('Akhal-Teke', 'Akhal-Teke'), ('Cuarto de milla', 'Cuarto de milla'), ('Appaloosa', 'Appaloosa'), ('Azteca', 'Azteca'), ('Paso Peruano', 'Paso Peruano'), ('Painted Horse', 'Painted Horse'), ('Paso Tennessee', 'Paso Tennessee'), ('Mustang', 'Mustang'), ('Shire', 'Shire'), ('Frisón', 'Frisón'), ('Percherón', 'Percherón'), ('Marwari', 'Marwari'), ('Lusitano', 'Lusitano'), ('Bretón', 'Bretón'), ('Bereber', 'Bereber'), ('Criollo', 'Criollo'), ('Gelder', 'Gelder'), ('Hannoveriano', 'Hannoveriano'), ('Hispano-Árabe', 'Hispano-Árabe'), ('Kentucky Mountain', 'Kentucky Mountain'), ('Lipizzano', 'Lipizzano'), ('Mongol', 'Mongol'), ('Morgan', 'Morgan'), ('Przewalski', 'Przewalski')], max_length=64)),
                ('gender', models.IntegerField(choices=[(0, 'Stallion'), (1, 'Gelding'), (2, 'Mare')])),
                ('years', models.IntegerField(validators=[sleipnir.api.models.validate_positive])),
                ('image', models.ImageField(upload_to='')),
                ('owner', models.ForeignKey(default=0, on_delete=django.db.models.deletion.CASCADE, related_name='rider_horse', to='api.rider')),
            ],
        ),
    ]
