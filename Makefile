
all:
	./gradlew build

dev:
	./gradlew setupDecompWorkspace idea

clean:	
	rm -rf build .gradle .idea run out *.iml *.ipr *.iws

rel.upload:
	docker build -t mekanica .
	docker run -a stdin -a stdout -a stderr -e CF_API_TOKEN \
		  -w /mekanica -it mekanica make docker.rel.upload

docker.rel.upload:
	python3 release.py \
	        -project=315844:mekanica: \
			-project=315908:mekanica-generators:mekanica \
			-project=315907:mekanica-tools:mekanica \
			-mcvsn 1.12.2 -rel release

rel:
	mkdir -p build.docker
	docker build -t mekanica .
	docker run -a stdin -a stdout -a stderr -e CF_API_TOKEN \
	      --mount type=bind,src=`pwd`/build.docker,dst=/build.docker \
		  -w /mekanica -it mekanica make docker.rel

docker.rel:
	python3 release.py \
	        -project=315844:mekanica: \
			-project=315908:mekanica-generators:mekanica \
			-project=315907:mekanica-tools:mekanica \
			-skipupload=true \
			-mcvsn 1.12.2 -rel release
	cp build/libs/* /build.docker
