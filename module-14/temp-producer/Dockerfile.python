FROM python:3.6.5-slim-stretch
WORKDIR /app
RUN pip install confluent-kafka
COPY . .
CMD python main.py 
