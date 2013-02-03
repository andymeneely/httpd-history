
# Script to accumulate LOCs for files at every Git revision
# Script must be run in Git Repository
# Contributors: Brian Spates

require "JSON"

if(ARGV.size != 1)
	puts "You must provide a file path to write the results to"
	exit
end
input = `git log --numstat`

commit_ids = Hash.new(0)
temp = ""

# Regex to find commit ids and .c and .h files within said commit.
# Creates a hash of commit ids with git repo file paths as values 
input.scan(/^commit \w+|^.*\d+\t\d+\t[\w+\b\/].*\.c$|^.*\d+\t\d+\t[\w+\b\/].*\.h$/) do | val |
	
		if(val.match(/commit/) != nil)
			val.gsub!(/commit/, "")
			val.strip!
			temp = val
			commit_ids[val] = Array.new(0)
		else
			val.gsub!(/\d+\t/, "")
			val.strip!
			commit_ids[temp] << val
		end
	
end

docObj = Hash.new(0)

# Iterate through commit ids to create a hash of file names with LOC for each revision
# Todo remove file paths from Hash that git checkout cannot locate due to deletion
commit_ids.each do | k, v |
	if(!v.empty?) 
		v.each { |file| docObj[file] = Array.new(0) }
		v.each do | file |
			`git checkout #{k} -- #{file}`
			response =  `../utils/cloc-1.56 #{file}`
			docObj[file] << k << response.match(/\d+$/).to_s
		end
	end
end

# create JSON file structured as filename: array of commit ids and LOC
document = JSON.pretty_generate(docObj)
File.open(ARGV[0], 'w') do |file|
	file.write(document)
end