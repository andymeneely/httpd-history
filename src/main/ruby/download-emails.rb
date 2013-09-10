#!/usr/bin/ruby

require 'mechanize'

a = Mechanize.new 

index_page = a.get('http://httpd.apache.org/mail/dev')

download_agent = Mechanize.new
download_agent.pluggable_parser.default = Mechanize::Download

index_page.links.each do |link|
  if link.text.end_with? '.gz'
    puts "Downloading #{link.text}..."
    url = "http://httpd.apache.org/mail/dev/#{link.uri}"
    archive = download_agent.get(url).save(link.text)
    
  end

end
