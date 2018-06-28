FROM python:3.6.5-slim-stretch
WORKDIR /app
COPY requirements.txt ./
RUN pip install -r requirements.txt
COPY . .
CMD python main.py 
