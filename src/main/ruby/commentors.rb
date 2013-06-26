#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'

opts = Trollop::options do
  banner "Get the commentors on all Bugzilla files"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :header, "Add a header to the top of the output", :default => false
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])

puts "Contributor Name\tContributor ID" if opts[:header]

commentors = []

Dir.chdir(opts[:xmls])
Dir.glob("*.xml") do |file|
  xml = Nokogiri::XML(File.open(file,"r"))
  #id = xml.at_xpath("//bug_id").content.to_i
  xml.xpath("//who").each do |c|
    commentors << ["#{c[:name]}", "#{c.content}"]
  end
end
commentors.uniq!

commentors.each do |arr|
  puts "#{arr[0]}\t#{arr[1]}"
end

