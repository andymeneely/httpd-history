#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'

opts = Trollop::options do
  banner "Count the number of comments in Bugzlla bugs stored in xml files"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :filter_non_humans, "Should the script filter out non-humans?", :default => false
  opt :header, "Show the column headers", :default => false
  opt :sample, "Show a sample comment from the contributor?", :default => false
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])

#puts "ID\tContributor Name\tContributor ID" if opts[:header]

commentors = {}

Dir.chdir(opts[:xmls])
Dir.glob("*.xml") do |file|
  xml = Nokogiri::XML(File.open(file,"r"))
  #id = xml.at_xpath("//bug_id").content.to_i
  xml.xpath("//who").each do |c|
    commentors[c.content] = {:name => c[:name]}
    commentors[c.content][:sample] = c.parent.at_xpath(".//thetext").content.gsub(/\n/,' ') if opts[:sample]
  end
end

commentors.each do |id, hash|
  puts "#{id}\t#{hash[:name]}\t#{hash[:sample]}"
end

