#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'

opts = Trollop::options do
  banner "Count the number of comments in Bugzlla bugs stored in xml files"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :output, "The output file of this data", :default => 'comment-counts.txt'
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])

Dir.chdir(opts[:xmls])

Dir.glob("*.xml") do |file|
  xml = Nokogiri::XML(File.open(file,"r"))
  id = xml.at_xpath("//bug_id").content.to_i
  num_comments = xml.xpath("//commentid").size
  puts "#{id}\t#{num_comments}"
end

