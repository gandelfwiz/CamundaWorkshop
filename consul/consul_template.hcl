template {
	source = "../nginx/conf/nginx-template.conf"
    destination = "../nginx/conf/consul-nginx.conf"
	command = "../nginx/nginx -s reload"
}

consul {
	address="localhost:8500"
	
	retry {
		enabled = true
		attempts = 10
		backoff = "300ms"
	}
}