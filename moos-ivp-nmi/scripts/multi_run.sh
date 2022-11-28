while read -r line
do
	python3 mission_generate.py $line
	cd ../missions/s3_uuvnmi
	./launch.sh
	cd ../../scripts
done < setups.txt