docker-compose -f docker-compose2.yml down
docker volume prune -f
docker network prune -f
docker ps -a
