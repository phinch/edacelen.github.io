import csv
import re

'''
TO USE: python decode.py <weather-file>
will output to clean.txt

decodes noaa files
docs at http://www1.ncdc.noaa.gov/pub/data/noaa/ish-format-document.pdf
weather currently determined by weather at tfgreen because providence 
tower doesn't record precipitation data

output:
chars 1-8 date YYYYMMDD
chars 9-12 time HHMM (military time)
chars 13-18 lat
chars 19-25 lon
char 26 precipitation (0=none, 4=heavy)

FILL IN BEGIN/END DATE TO FILTER DATA: inclusive YYYYMMDD
'''
begin_date = 20160227
end_date = 20160306

with open('725070-14765-2016', 'rb') as fin:
	with open('clean.txt', 'wb') as fout:
		for line in fin:
			# 16-23 date YYYYMMDD 20160101
			date = int(line[15:23])

			if date >= begin_date and date <= end_date:
				# 24-27 time HHMM, 00 < HH < 23 0000
				time = line[23:27]

				# 29-34 latitude +/-ddddd +41400 - +42000
				lat = line[28:34]

				# 35-41 longitude +/-ddddd -071320 - -071433
				lon = line[34:41]

				# 66-69 wind speed (meters/sec) scale x10
				# 71-75 height above ground of lowest cloud (meters)
				# 88-92 air temperature +/-dddd (C) scale x10

				# additional information (occurs after chars 'ADD')
				# r'AA\d' indicates precipitation
				## next 2 chars - num hours measured
				## next 4 chars - mm of rain x10
				# r'AW\d' reports present weather conditions
				precip = 0

				if re.search(r'AA\d', line) is not None:
					mm = re.search(r'AA\d\d\d\d\d\d\d', line).group()[5:]
					mm = int(mm) * 10

					if mm > 600:
						precip = 4
					elif mm > 200:
						precip = 3
					elif mm > 0:
						precip = 2
					else:
						precip = 1 

				if precip > 0:
					precip = 1
				fout.write(str(date)+time+lat+lon+str(precip)+',')
