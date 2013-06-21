#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'
require 'base64'

opts = Trollop::options do
  banner "Count the number of comments in Bugzlla bugs stored in xml files"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :min_comments, "The minimum number of comments to show", :default => 0
  opt :min_commentors, "The minimum number of unique commentators on the bug", :default => 0
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])
Trollop::die "min-comments must be 0 or greater" unless opts[:min_comments]>=0
Trollop::die "min-commentors must be 0 or greater" unless opts[:min_commentors]>=0

Dir.chdir(opts[:xmls])

Dir.glob("*.xml") do |file|
  xml = Nokogiri::XML(File.open(file,"r"))
  comment_ids = xml.xpath("//commentid")
  commentor_nodes = xml.xpath("//who")
  commentors = []
  commentor_nodes.each { |c| commentors << c.content }
  commentors.uniq!
  
  next if commentors.size < opts[:min_commentors] || comment_ids.size < opts[:min_comments]
  
  # Query the XML for stuff
  id = xml.at_xpath("//bug_id").content.to_i
  patches = xml.xpath("//attachment[@ispatch=1]")
  votes = xml.at_xpath("//votes").content.to_i
  
  # Count the number of files for each patch
  patch_files = []
  patches.each do |node|
     Base64.decode64(node.at_xpath("//data").content).each_line do |line| 
       patch_files << line.split("\t")[0].split(" ")[1] if line.start_with? "+++ "
     end
  end
  patch_files.uniq!

  # Print to console!
  puts "#{id}\t#{comment_ids.size}\t#{commentors.size}\t#{votes}\t#{patches.size}\t#{patch_files.size}"
  
end
#puts "ID\tCommments\tCommentors\tVotes\tPatches\tFiles in Patches"

