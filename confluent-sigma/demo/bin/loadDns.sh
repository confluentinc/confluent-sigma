docker run -v $(pwd)/demo/data:/tmp/data -it --network=host edenhill/kcat:1.7.1 -b localhost:9092 -t dns -P -l /tmp/data/dns.txt 
