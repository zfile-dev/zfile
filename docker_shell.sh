docker build -t zfile:2.7 .

docker run -d -p 0.0.0.0:8080:8080 --name=zfile --restart=always zfile:2.7
#docker logs --tail=111 -f 3c5a32c9fe7d19be7b51a9be329e11f286731f8c120bc58edb2872872066e474
