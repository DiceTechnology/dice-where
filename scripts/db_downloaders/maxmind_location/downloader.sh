#!/usr/bin/env bash

source $(dirname $0)/../functions.sh

init_maxmind_location_download_arguments () {
	if [ $# -eq 1 ]; then
		if [ -f $1 ]; then
			. $1
		else
			echo "ERROR: passed argument is not a valid file"
			exit 1;
		fi
	elif [ $# -eq 4 ]; then
		maxmind_location_product=$1;
		maxmind_location_content=$2;
		maxmind_location_licence_key=$3;
		maxmind_location_zip_destination=$4;
	else
		echo "ERROR: Improper usage";
		echo "Usage 1)";
		echo "./location_donwloader.sh config.sh";
		echo "Usage 2)";
		echo "./location_donwloader.sh <product> <content> <licence_key> <zip_destination>";
		exit 1;
	fi

	if [ -z "$maxmind_location_product" ]; then
		echo "ERROR: maxmind_location_product is not defined"
		exit 1;
	fi;

	if [ -z "$maxmind_location_content" ]; then
		echo "ERROR: maxmind_location_content is not defined"
		exit 1;
	fi;

	if [ -z "$maxmind_location_licence_key" ]; then
		echo "ERROR: maxmind_location_licence_key is not defined"
		exit 1;
	fi;

	if [ -z "$maxmind_location_zip_destination" ] || [ ! -d "$maxmind_location_zip_destination" ]; then
		echo "ERROR: maxmind_location_zip_destination needs to be a valid directory"
		exit 1;
	fi;
}

init_maxmind_location_download_arguments $@

zip_url="https://download.maxmind.com/app/geoip_download?edition_id=${maxmind_location_product}-${maxmind_location_content}-CSV&license_key=${maxmind_location_licence_key}&suffix=zip"
zip_file_destination="${maxmind_location_zip_destination}/${maxmind_location_product}-${maxmind_location_content}-CSV-latest.zip"
unzip_destination_directory="${maxmind_location_zip_destination}/${maxmind_location_product}-${maxmind_location_content}-CSV-latest"


download_maxmind_db ${zip_url} ${zip_file_destination} ${unzip_destination_directory}




