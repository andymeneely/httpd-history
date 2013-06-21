#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'

opts = Trollop::options do
  banner "Count the number of comments in Bugzlla bugs stored in xml files"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :min, "The minimum number of comments to show", :default => 0
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])
Trollop::die "min must be 0 or greater" unless opts[:min]>=0

Dir.chdir(opts[:xmls])

Dir.glob("*.xml") do |file|
  xml = Nokogiri::XML(File.open(file,"r"))
  id = xml.at_xpath("//bug_id").content.to_i
  num_comments = xml.xpath("//commentid").size
  puts "#{id}\t#{num_comments}" if num_comments >= opts[:min]
end

