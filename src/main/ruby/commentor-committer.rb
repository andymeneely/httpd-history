#!/usr/bin/env ruby
require 'nokogiri'
require 'trollop'
require 'csv'

opts = Trollop::options do
  banner "Figure out if the commentor is also a committer"
  opt :xmls, "The xml/ directory of Bugzillas", :default => './xml/'
  opt :committers, "The file of committers, from e.g. git log --pretty=\"%aN%x09%aE | sort | uniq", :default => "committers.txt"
  opt :header, "Add a header to the top of the output", :default => false
end

Trollop::die "xml directory must be a directory" unless Dir.exists?(opts[:xmls])
Trollop::die "committers file does not exist" unless File.exists? opts[:committers]

puts "Contributor Name\tContributor ID" if opts[:header]

def get_commentors(opts)
  commentors = []
  Dir.chdir(opts[:xmls]) do
    Dir.glob("*.xml") do |file|
      xml = Nokogiri::XML(File.open(file,"r"))
      xml.xpath("//who").each do |c|
        commentors << ["#{c[:name]}", "#{c.content}"] # name, ID
      end
    end
  end
  commentors.uniq
end 

def get_committers(opts)
  committers = []
  CSV.foreach(opts[:committers],{:col_sep=>"\t"}) do |c|
    committers << [ "#{c[0]}", "#{c[1]}" ]
  end
  committers.uniq
end

commentors = get_commentors(opts)
committers = get_committers(opts)
committers.each do |committer|
  name = committer[0]
  email = committer[1]
  potential_user = email.split("@")[0]
  commentors.each do |commentor|
    if commentor[0].eql?(potential_user)
      puts "#{name}\t#{email}\t--> #{commentor[0]}\t#{commentor[1]}" 
    elsif commentor[1].eql?(potential_user)
      puts "#{name}\t#{email}\t--> #{commentor[0]}\t#{commentor[1]}"
    end
  end
end

#commentors.each do |arr|
#  puts "#{arr[0]}\t#{arr[1]}"
#end

