#!/usr/bin/env bash

source $(dirname $0)/../functions.sh

init_maxmind_location_download_arguments $@

zip_url="https://download.maxmind.com/app/geoip_download?edition_id=${maxmind_location_product}-${maxmind_location_content}-CSV&license_key=${maxmind_location_licence_key}&suffix=zip"
zip_file_destination="${maxmind_location_zip_destination}/${maxmind_location_product}-${maxmind_location_content}-CSV-latest.zip"
unzip_destination_directory="${maxmind_location_zip_destination}/${maxmind_location_product}-${maxmind_location_content}-CSV-latest"
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



