
all:
	./gradlew build

dev:
	./gradlew setupDecompWorkspace idea

clean:	
	rm -rf build .gradle .idea run out *.iml *.ipr *.iws

rel.upload:
	docker build -t mekanism .
	docker run -a stdin -a stdout -a stderr -e CF_API_TOKEN \
		  -w /mekanism -it mekanism make docker.rel.upload

docker.rel.upload:
	python3 release.py \
	        -project=315844:mekanism: \
			-project=315908:mekanism-generators:mekanism \
			-project=315907:mekanism-tools:mekanism \
			-mcvsn 1.12.2 -rel release

rel:
	mkdir -p build.docker
	docker build -t mekanism .
	docker run -a stdin -a stdout -a stderr -e CF_API_TOKEN \
	      --mount type=bind,src=`pwd`/build.docker,dst=/build.docker \
		  -w /mekanism -it mekanism make docker.rel

docker.rel:
	python3 release.py \
	        -project=315844:mekanism: \
			-project=315908:mekanism-generators:mekanism \
			-project=315907:mekanism-tools:mekanism \
			-skipupload=true \
			-mcvsn 1.12.2 -rel release
	cp build/libs/* /build.docker
