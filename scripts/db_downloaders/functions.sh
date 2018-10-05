#!/usr/bin/env bash

download_maxmind_db () {
	zip_url=$1
	zip_file_destination=$2
	unzip_destination_directory=$3

	echo "Will download ${zip_url} to ${zip_file_destination}"
	wget -O ${zip_file_destination} ${zip_url}
	echo "Finished downloading ${zip_url}"
	echo ""

	inzip_path="$(unzip -l ${zip_file_destination} | sed -n 4p | awk '{split($4,path,"/"); print path[1]}')"
	db_ts="$(unzip -l ${zip_file_destination} | sed -n 4p | awk '{split($4,path,"/"); print path[1]}' | awk '{ split($1,ts,"_"); print ts[2]}')"

	echo "Will unzip ${zip_file_destination} into ${unzip_destination_directory}"
	unzip -q -o ${zip_file_destination} -d ${unzip_destination_directory}
	mv -f ${unzip_destination_directory}/${inzip_path}/* ${unzip_destination_directory}
	rm -fr ${unzip_destination_directory}/${inzip_path}
	echo ${db_ts} > ${unzip_destination_directory}/timestamp
	echo "Finished unzipping database"

	rm -f ${zip_file_destination}
	echo "Deleted the downloaded zip"
}