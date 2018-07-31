docker-compose down
docker-compose -f docker-compose.apps.yml down
docker volume prune -f
docker network prune -f
docker ps -a
